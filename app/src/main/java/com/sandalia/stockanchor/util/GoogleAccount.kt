package com.sandalia.stockanchor.util

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.security.MessageDigest

/**
 * Google Play Billing doesn't tell apps which account pays, so the address comes from Android's own account picker.
 * It lists the Google accounts on this device, needs no permission, and the person can't type a different address.
 */
object GoogleAccount {
    private const val GOOGLE_ACCOUNT_TYPE = "com.google"

    fun chooserIntent(description: String) = AccountManager.newChooseAccountIntent(
        null, null, arrayOf(GOOGLE_ACCOUNT_TYPE), description, null, null, null,
    )

    /**
     * Sent to Google Play as obfuscatedAccountId (at most 64 characters, must not reveal the address), so the server
     * can check that a purchase belongs to the address the account is created for. Same hash as the Laravel API.
     */
    fun purchaseId(email: String): String = MessageDigest.getInstance("SHA-256")
        .digest(email.trim().lowercase().toByteArray())
        .joinToString("") { "%02x".format(it) }
}

/** Returns a function that opens the picker; [onPicked] gets the chosen address, nothing happens if it is dismissed. */
@Composable
fun rememberGoogleAccountPicker(description: String, onPicked: (String) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (result.resultCode == Activity.RESULT_OK && !email.isNullOrBlank()) latest.value(email)
    }
    return { runCatching { launcher.launch(GoogleAccount.chooserIntent(description)) } }
}
