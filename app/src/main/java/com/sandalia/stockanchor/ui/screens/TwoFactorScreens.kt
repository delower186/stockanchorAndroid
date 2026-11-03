package com.sandalia.stockanchor.ui.screens

import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sandalia.stockanchor.data.remote.TwoFactorSetup
import com.sandalia.stockanchor.data.remote.TwoFactorStatus
import com.sandalia.stockanchor.ui.components.LocalContainer
import com.sandalia.stockanchor.ui.components.AppCard
import com.sandalia.stockanchor.ui.components.Runner

/* ============================================================ Two-step verification */

/** Second sign-in step: a code from the authenticator app, or a recovery code. */
@Composable
fun TwoFactorCodeDialog(busy: Boolean, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Shield, null) },
        title = { Text(str(R.string.two_step_verification)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (recovery) str(R.string.enter_one_of_the_recovery_codes)
                    else str(R.string.enter_the_6_digit_code_from),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    code, { code = if (recovery) it.take(20) else it.filter(Char::isDigit).take(6) },
                    label = { Text(if (recovery) str(R.string.recovery_code) else str(R.string.authentication_code)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = if (recovery) KeyboardType.Ascii else KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { recovery = !recovery; code = "" }) {
                    Text(if (recovery) str(R.string.use_the_authenticator_app) else str(R.string.use_a_recovery_code))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(code) }, enabled = !busy && (if (recovery) code.length >= 8 else code.length == 6)) { Text(str(R.string.verify)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(str(R.string.cancel)) } },
    )
}

/** Turning two-step verification on / off for the signed-in account (Plan & account). */
@Composable
fun TwoFactorCard(runner: Runner) {
    val container = LocalContainer.current
    val context = LocalContext.current
    var status by remember { mutableStateOf<TwoFactorStatus?>(null) }
    var askPassword by remember { mutableStateOf<String?>(null) } // "on", "codes", "off"
    var setup by remember { mutableStateOf<TwoFactorSetup?>(null) }
    var recoveryCodes by remember { mutableStateOf<List<String>?>(null) }

    LaunchedEffect(Unit) { status = runCatching { container.account.twoFactorStatus() }.getOrNull() }

    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Shield, null, tint = if (status?.enabled == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(str(R.string.two_step_verification), style = MaterialTheme.typography.titleMedium)
                Text(
                    when (status?.enabled) {
                        true -> str(R.string.on_x_recovery_codes_left, status?.recoveryCodesLeft)
                        false -> str(R.string.off_ask_for_a_code_from)
                        null -> str(R.string.checking)
                    },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when (status?.enabled) {
            false -> Button(onClick = { askPassword = "on" }, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.set_up)) }
            true -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { askPassword = "codes" }, modifier = Modifier.weight(1f)) { Text(str(R.string.new_recovery_codes)) }
                TextButton(onClick = { askPassword = "off" }) { Text(str(R.string.turn_off), color = MaterialTheme.colorScheme.error) }
            }
            null -> Unit
        }
    }

    askPassword?.let { purpose ->
        PasswordDialog(
            title = when (purpose) { "on" -> str(R.string.set_up_two_step_verification); "codes" -> str(R.string.new_recovery_codes); else -> str(R.string.turn_off_two_step_verification) },
            message = when (purpose) {
                "on" -> str(R.string.confirm_your_password_to_continue)
                "codes" -> str(R.string.your_current_recovery_codes_will_stop)
                else -> str(R.string.anyone_with_your_password_could_then)
            },
            busy = runner.busy,
            onDismiss = { askPassword = null },
            onConfirm = { password ->
                runner.run(if (purpose == "off") str(R.string.two_step_verification_is_off) else null) {
                    when (purpose) {
                        "on" -> setup = container.account.beginTwoFactor(password)
                        "codes" -> recoveryCodes = container.account.newRecoveryCodes(password)
                        else -> container.account.disableTwoFactor(password)
                    }
                    askPassword = null
                    status = container.account.twoFactorStatus()
                }
            },
        )
    }

    setup?.let { s ->
        TwoFactorSetupDialog(
            setup = s, busy = runner.busy,
            onOpenApp = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(s.otpauthUrl)))
                } catch (e: ActivityNotFoundException) {
                    runner.toast(str(R.string.install_google_authenticator_or_a_similar))
                }
            },
            onConfirm = { code ->
                runner.run(str(R.string.two_step_verification_is_on)) {
                    recoveryCodes = container.account.confirmTwoFactor(code)
                    setup = null
                    status = container.account.twoFactorStatus()
                }
            },
            onDismiss = { setup = null },
        )
    }

    recoveryCodes?.let { RecoveryCodesDialog(it, onDismiss = { recoveryCodes = null }) }
}

@Composable
private fun TwoFactorSetupDialog(setup: TwoFactorSetup, busy: Boolean, onOpenApp: () -> Unit, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        title = { Text(str(R.string.set_up_two_step_verification)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(str(R.string.s_1_add_stock_anchor_to_google), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onOpenApp, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text(str(R.string.open_authenticator_app))
                }
                Text(str(R.string.or_enter_this_key_in_the), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(setup.secret.chunked(4).joinToString(" "), fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    TextButton(onClick = { clipboard.setText(AnnotatedString(setup.secret)) }) {
                        Icon(Icons.Filled.ContentCopy, null); Spacer(Modifier.width(4.dp)); Text(str(R.string.copy))
                    }
                }
                Text(str(R.string.s_2_enter_the_6_digit_code), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    code, { code = it.filter(Char::isDigit).take(6) }, label = { Text(str(R.string.code)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(code) }, enabled = !busy && code.length == 6) { Text(str(R.string.turn_on)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(str(R.string.cancel)) } },
    )
}

@Composable
private fun RecoveryCodesDialog(codes: List<String>, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = {},
        title = { Text(str(R.string.save_your_recovery_codes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str(R.string.if_you_lose_your_phone_each),
                    style = MaterialTheme.typography.bodyMedium)
                codes.chunked(2).forEach { pair ->
                    Row { pair.forEach { Text(it, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)) } }
                }
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(codes.joinToString("\n"))); copied = true }) {
                    Icon(Icons.Filled.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text(if (copied) str(R.string.copied) else str(R.string.copy_codes))
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(str(R.string.i_saved_them)) } },
    )
}

@Composable
private fun PasswordDialog(title: String, message: String, busy: Boolean, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    password, { password = it }, label = { Text(str(R.string.your_password)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(password) }, enabled = !busy && password.isNotEmpty()) { Text(str(R.string.continue_label)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(str(R.string.cancel)) } },
    )
}
