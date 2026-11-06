package com.sandalia.stockanchor.data.repo

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.os.Build
import com.sandalia.stockanchor.BuildConfig
import com.sandalia.stockanchor.data.prefs.AppMode
import com.sandalia.stockanchor.data.prefs.SessionStore
import com.sandalia.stockanchor.data.remote.ApiClient
import com.sandalia.stockanchor.data.remote.ApiException
import com.sandalia.stockanchor.data.remote.AuthResponse
import com.sandalia.stockanchor.data.remote.CodeRequest
import com.sandalia.stockanchor.data.remote.LoginRequest
import com.sandalia.stockanchor.data.remote.PasswordRequest
import com.sandalia.stockanchor.data.remote.PromoRequest
import com.sandalia.stockanchor.data.remote.RegisterCodeRequest
import com.sandalia.stockanchor.data.remote.RegisterCodeResponse
import com.sandalia.stockanchor.data.remote.RegisterRequest
import com.sandalia.stockanchor.data.remote.TwoFactorChallengeRequest
import com.sandalia.stockanchor.data.remote.TwoFactorSetup
import com.sandalia.stockanchor.data.remote.TwoFactorStatus
import com.sandalia.stockanchor.data.remote.VerifyPurchaseRequest

/** Result of switching a device to the cloud after login/upgrade. */
sealed interface ActivationResult {
    data object Activated : ActivationResult
    /** The cloud account already has data and this device has local data too — ask the user. */
    data object NeedsMergeDecision : ActivationResult
    data object NotPremium : ActivationResult
}

/**
 * Basic → Premium upgrade, cloud sign-in and the reverse downgrade.
 *
 *  Upgrade:  Play purchase or promo code → (sendRegistrationCode + register | login) → activateCloud()
 *  New device / team member: login() → activateCloud()
 *  The server creates an account or issues a token only together with an active Premium plan.
 *  Lapsed subscription: downgradeToBasic() keeps every record on the device, premium screens lock.
 */
class AccountManager(
    private val api: ApiClient,
    private val session: SessionStore,
    private val sync: SyncManager,
) {
    private val deviceName get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    /**
     * Password sign-in, refused by the server (code premium_required, nothing saved here) without an active plan.
     * Any payment or promo code waiting on this device is sent along and activates the plan.
     * Returns a two-step verification challenge when the account has it on; finish with [completeTwoFactor].
     * Returns null when signed in.
     */
    suspend fun login(email: String, password: String): String? {
        val r = api.call {
            login(LoginRequest(email.trim(), password, deviceName, true, session.pendingPurchaseToken(), session.pendingPromo(), productIdIfPurchase(), session.pendingPlan()))
        }
        if (r.twoFactorRequired == true) return r.challenge
        storeSignedIn(r)
        return null
    }

    /** Code from the authenticator app (or a recovery code) for the challenge returned by [login]. */
    suspend fun completeTwoFactor(challenge: String, code: String) {
        storeSignedIn(api.call {
            twoFactorChallenge(TwoFactorChallengeRequest(challenge, code.trim(), true, session.pendingPurchaseToken(), session.pendingPromo(), productIdIfPurchase(), session.pendingPlan()))
        })
    }

    /** Emails the verification code. The server only does this when the waiting payment or promo code is valid. */
    suspend fun sendRegistrationCode(email: String): RegisterCodeResponse = api.call {
        sendRegistrationCode(RegisterCodeRequest(email.trim(), session.pendingPurchaseToken(), session.pendingPromo(), productIdIfPurchase(), session.pendingPlan()))
    }

    /** Creates the account with the emailed code; Premium is active in the same step or nothing is created. */
    suspend fun register(name: String, email: String, password: String, company: String, currency: String, emailCode: String): AuthResponse {
        val r = api.call {
            register(RegisterRequest(name.trim(), email.trim(), password, company.trim(), currency, deviceName, emailCode.trim(), session.pendingPurchaseToken(), session.pendingPromo(), productIdIfPurchase(), session.pendingPlan()))
        }
        storeSignedIn(r)
        return r
    }

    /** Checks a promo code before a new customer is asked to create an account; throws when it can't be used. */
    suspend fun checkPromo(code: String) {
        val checked = api.call { checkPromo(PromoRequest(code.trim())) }
        session.setPendingPromo(checked.code ?: code.trim())
    }

    /** Promo code for the signed-in company. Returns true when Premium is active. */
    suspend fun redeemPromo(code: String): Boolean {
        val r = api.call { redeemPromo(PromoRequest(code.trim())) }
        store(r)
        return r.subscription?.isPremium == true
    }

    /** A session on this device without Premium: drop it, keeping every local record (unlike [signOut]). */
    suspend fun discardUnpaidSession() {
        runCatching { api.call { logout() } }
        session.clearCloud()
    }

    private fun productIdIfPurchase() = BuildConfig.SUBSCRIPTION_PRODUCT_ID.takeIf { session.pendingPurchaseToken() != null }

    private fun storeSignedIn(r: AuthResponse) {
        store(r)
        if (r.subscription?.isPremium == true) session.clearPendingProof()
    }

    /* ---- Two-step verification for the signed-in account ---- */

    suspend fun twoFactorStatus(): TwoFactorStatus = api.call { twoFactorStatus() }

    suspend fun beginTwoFactor(password: String): TwoFactorSetup = api.call { enableTwoFactor(PasswordRequest(password)) }

    /** Returns the recovery codes. */
    suspend fun confirmTwoFactor(code: String): List<String> = api.call { confirmTwoFactor(CodeRequest(code.trim())) }.recoveryCodes

    suspend fun newRecoveryCodes(password: String): List<String> = api.call { newRecoveryCodes(PasswordRequest(password)) }.recoveryCodes

    suspend fun disableTwoFactor(password: String) {
        api.call { disableTwoFactor(PasswordRequest(password)) }
    }

    /** Sends the Google Play purchase token (saved by BillingManager) to the server for verification. */
    suspend fun verifyPendingPurchase(): Boolean {
        val token = session.pendingPurchaseToken() ?: return session.current.isPremium
        val r = api.call {
            verifyPurchase(VerifyPurchaseRequest(token, BuildConfig.SUBSCRIPTION_PRODUCT_ID, session.pendingPlan()))
        }
        store(r)
        if (r.subscription?.isPremium == true) session.setPendingPurchase(null, null)
        return r.subscription?.isPremium == true
    }

    /** Verifies an arbitrary token (Restore purchases). */
    suspend fun verifyToken(token: String, plan: String?): Boolean {
        session.setPendingPurchase(token, plan)
        return verifyPendingPurchase()
    }

    /**
     * Switches this device to cloud mode. When [upload] is true the local Basic database is pushed first
     * (first upgrade). [merge] forces the upload even if the cloud already has products.
     */
    suspend fun activateCloud(upload: Boolean, merge: Boolean = false): ActivationResult {
        if (!session.current.isPremium) return ActivationResult.NotPremium
        if (upload && sync.hasLocalData()) {
            try {
                sync.uploadLocal(merge)
            } catch (e: ApiException) {
                if (e.code == "cloud_not_empty") return ActivationResult.NeedsMergeDecision
                throw e
            }
        }
        session.setMode(AppMode.CLOUD)
        session.setLastSync(null)
        sync.pull(full = true)
        return ActivationResult.Activated
    }

    /** Refreshes plan & permissions. Returns false when the subscription is no longer active. */
    suspend fun refresh(): Boolean {
        if (session.token() == null) return false
        val r = api.call { me() }
        store(r)
        return r.subscription?.isPremium == true
    }

    /** Keep working offline on Basic with everything that was synced. */
    fun downgradeToBasic() {
        session.clearCloud()
    }

    /** Serialises sign-out paths so a failing logout call (401) can't trigger a second reset. */
    private val signOutLock = Mutex()

    /** Sign out of the cloud. The cached team data is removed from this device. */
    suspend fun signOut() = signOutLock.withLock {
        runCatching { api.call { logout() } }
        session.clearCloud()
        sync.resetLocal()
    }

    /**
     * The server no longer accepts this device's session (token expired or revoked, account disabled,
     * company suspended): same result as signing out, without calling the server.
     * Returns false when there was no session to end (e.g. several requests failed at once).
     */
    suspend fun endSession(): Boolean = signOutLock.withLock {
        if (session.token() == null) return@withLock false
        session.clearCloud()
        sync.resetLocal()
        true
    }

    private fun store(r: AuthResponse) {
        val u = r.user
        if (u != null) {
            session.saveAuth(
                token = r.token,
                name = u.name.orEmpty(),
                email = u.email.orEmpty(),
                role = u.role ?: "staff",
                permissions = u.permissions.orEmpty(),
                company = r.team?.name,
                currency = r.team?.currency,
            )
        }
        r.subscription?.let { session.saveSubscription(it.isPremium == true, it.status, it.plan, it.expiresAt) }
    }
}
