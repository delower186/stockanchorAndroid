package com.sandalia.stockanchor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.sandalia.stockanchor.BuildConfig
import com.sandalia.stockanchor.domain.Perm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppMode { LOCAL, CLOUD }

/** Everything the UI needs to know about who is using the app and on which plan. */
data class Session(
    val mode: AppMode = AppMode.LOCAL,
    val isPremium: Boolean = false,
    val loggedIn: Boolean = false,
    val userName: String = "",
    val email: String = "",
    val role: String = "owner",
    val permissions: Set<String> = Perm.ALL.toSet(),
    val companyName: String = "My Business",
    val currency: String = "৳",
    val plan: String? = null,
    val expiresAt: String? = null,
    val subscriptionStatus: String? = null,
    val lastSync: String? = null,
    val lowStockAlerts: Boolean = true,
    val onboarded: Boolean = false,
) {
    val isCloud get() = mode == AppMode.CLOUD
    /** Premium features are unlocked only in cloud mode with an active subscription. */
    val premiumActive get() = isCloud && isPremium
    /**
     * A signed-in user (owner or team member) is always held to the permissions the server gave them, even if the
     * device is still in local mode. Only an anonymous Basic install, i.e. a single owner, gets everything.
     */
    fun can(permission: String) = if (loggedIn) permission in permissions else mode == AppMode.LOCAL
}

/**
 * SharedPreferences-backed session. Synchronous getters are used by the OkHttp interceptor,
 * the StateFlow drives Compose.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("stock_anchor_session", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<Session> = _state.asStateFlow()
    val current: Session get() = _state.value

    fun token(): String? = prefs.getString(K_TOKEN, null)
    /** Release builds always use the built-in server; a URL saved by a debug build is ignored. */
    fun baseUrl(): String = prefs.getString(K_BASE_URL, null)?.takeIf { BuildConfig.ALLOW_SERVER_OVERRIDE && it.isNotBlank() }
        ?: BuildConfig.API_BASE_URL
    fun pendingPurchaseToken(): String? = prefs.getString(K_PENDING_TOKEN, null)
    /** The Play subscription to sell: the latest from the server, else the one built into the app. */
    fun subscriptionProductId(): String = prefs.getString(K_SUBSCRIPTION_PRODUCT, null) ?: BuildConfig.SUBSCRIPTION_PRODUCT_ID
    fun setSubscriptionProductId(id: String) = update { putString(K_SUBSCRIPTION_PRODUCT, id) }
    fun pendingPlan(): String? = prefs.getString(K_PENDING_PLAN, null)
    /** Promo code accepted on the paywall, waiting for the account it will activate. */
    fun pendingPromo(): String? = prefs.getString(K_PENDING_PROMO, null)
    /** Payment or promo code that hasn't been linked to an account yet. Create Account is only offered with one. */
    fun hasPendingProof(): Boolean = pendingPurchaseToken() != null || pendingPromo() != null
    /** Google account chosen on the paywall: the address a new cloud account is created for. */
    fun billingEmail(): String? = prefs.getString(K_BILLING_EMAIL, null)

    fun setBaseUrl(url: String) = update { putString(K_BASE_URL, url.trim()) }

    fun setPendingPurchase(token: String?, plan: String?) = update {
        putString(K_PENDING_TOKEN, token); putString(K_PENDING_PLAN, plan)
    }

    fun setPendingPromo(code: String?) = update { putString(K_PENDING_PROMO, code) }

    fun setBillingEmail(email: String?) = update { putString(K_BILLING_EMAIL, email) }

    fun clearPendingProof() = update {
        remove(K_PENDING_TOKEN); remove(K_PENDING_PLAN); remove(K_PENDING_PROMO); remove(K_BILLING_EMAIL)
    }

    fun saveAuth(
        token: String?, name: String, email: String, role: String, permissions: List<String>,
        company: String?, currency: String?,
    ) = update {
        if (token != null) putString(K_TOKEN, token)
        putString(K_USER, name); putString(K_EMAIL, email); putString(K_ROLE, role)
        putStringSet(K_PERMS, permissions.toSet())
        company?.let { putString(K_COMPANY, it) }
        currency?.let { putString(K_CURRENCY, symbolFor(it)) }
    }

    fun saveSubscription(isPremium: Boolean, status: String?, plan: String?, expiresAt: String?) = update {
        putBoolean(K_PREMIUM, isPremium); putString(K_SUB_STATUS, status); putString(K_PLAN, plan); putString(K_EXPIRES, expiresAt)
    }

    fun setMode(mode: AppMode) = update { putString(K_MODE, mode.name) }
    fun setLastSync(serverTime: String?) = update { putString(K_LAST_SYNC, serverTime) }
    fun setCompany(name: String, currencySymbol: String) = update { putString(K_COMPANY, name); putString(K_CURRENCY, currencySymbol) }
    fun setLowStockAlerts(enabled: Boolean) = update { putBoolean(K_ALERTS, enabled) }
    fun setOnboarded() = update { putBoolean(K_ONBOARDED, true) }

    /** Signs out of the cloud and returns to Basic (local) mode. */
    fun clearCloud() = update {
        remove(K_TOKEN); remove(K_USER); remove(K_EMAIL); remove(K_ROLE); remove(K_PERMS)
        remove(K_PREMIUM); remove(K_SUB_STATUS); remove(K_PLAN); remove(K_EXPIRES); remove(K_LAST_SYNC)
        putString(K_MODE, AppMode.LOCAL.name)
    }

    private fun update(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit(commit = true) { block() }
        _state.value = read()
    }

    private fun read(): Session {
        val loggedIn = prefs.getString(K_TOKEN, null) != null
        return Session(
            mode = runCatching { AppMode.valueOf(prefs.getString(K_MODE, AppMode.LOCAL.name)!!) }.getOrDefault(AppMode.LOCAL),
            isPremium = prefs.getBoolean(K_PREMIUM, false),
            loggedIn = loggedIn,
            userName = prefs.getString(K_USER, "") ?: "",
            email = prefs.getString(K_EMAIL, "") ?: "",
            role = prefs.getString(K_ROLE, "owner") ?: "owner",
            permissions = prefs.getStringSet(K_PERMS, null) ?: Perm.ALL.toSet(),
            companyName = prefs.getString(K_COMPANY, "My Business") ?: "My Business",
            currency = prefs.getString(K_CURRENCY, "৳") ?: "৳",
            plan = prefs.getString(K_PLAN, null),
            expiresAt = prefs.getString(K_EXPIRES, null),
            subscriptionStatus = prefs.getString(K_SUB_STATUS, null),
            lastSync = prefs.getString(K_LAST_SYNC, null),
            lowStockAlerts = prefs.getBoolean(K_ALERTS, true),
            onboarded = prefs.getBoolean(K_ONBOARDED, false),
        )
    }

    companion object {
        private const val K_MODE = "mode"
        private const val K_TOKEN = "token"
        private const val K_USER = "user_name"
        private const val K_EMAIL = "email"
        private const val K_ROLE = "role"
        private const val K_PERMS = "permissions"
        private const val K_COMPANY = "company"
        private const val K_CURRENCY = "currency"
        private const val K_PREMIUM = "premium"
        private const val K_SUB_STATUS = "sub_status"
        private const val K_PLAN = "plan"
        private const val K_EXPIRES = "expires"
        private const val K_LAST_SYNC = "last_sync"
        private const val K_ALERTS = "low_stock_alerts"
        private const val K_BASE_URL = "base_url"
        private const val K_PENDING_TOKEN = "pending_purchase_token"
        private const val K_PENDING_PLAN = "pending_plan"
        private const val K_PENDING_PROMO = "pending_promo"
        private const val K_BILLING_EMAIL = "billing_email"
        private const val K_SUBSCRIPTION_PRODUCT = "subscription_product_id"
        private const val K_ONBOARDED = "onboarded"

        fun symbolFor(code: String): String = when (code.uppercase()) {
            "BDT" -> "৳"; "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "INR" -> "₹"; "PKR" -> "Rs"
            else -> code
        }

        const val SUPPORTED_SYMBOLS = "৳ $ € £ ₹ Rs"

        /** The server's currency code for a typed symbol or code, or null when the server doesn't have it. */
        fun codeFor(symbol: String): String? = when (symbol.trim().uppercase()) {
            "৳", "BDT" -> "BDT"; "$", "USD" -> "USD"; "€", "EUR" -> "EUR"; "£", "GBP" -> "GBP"; "₹", "INR" -> "INR"; "RS", "₨", "PKR" -> "PKR"
            else -> null
        }
    }
}
