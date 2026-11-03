package com.sandalia.stockanchor.ui.screens

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
        title = { Text("Two-step verification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (recovery) "Enter one of the recovery codes you saved. Each code works once."
                    else "Enter the 6-digit code from your authenticator app.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    code, { code = if (recovery) it.take(20) else it.filter(Char::isDigit).take(6) },
                    label = { Text(if (recovery) "Recovery code" else "Authentication code") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = if (recovery) KeyboardType.Ascii else KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { recovery = !recovery; code = "" }) {
                    Text(if (recovery) "Use the authenticator app" else "Use a recovery code")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(code) }, enabled = !busy && (if (recovery) code.length >= 8 else code.length == 6)) { Text("Verify") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
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
                Text("Two-step verification", style = MaterialTheme.typography.titleMedium)
                Text(
                    when (status?.enabled) {
                        true -> "On · ${status?.recoveryCodesLeft} recovery codes left"
                        false -> "Off · ask for a code from an authenticator app at sign-in"
                        null -> "Checking…"
                    },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when (status?.enabled) {
            false -> Button(onClick = { askPassword = "on" }, modifier = Modifier.fillMaxWidth()) { Text("Set up") }
            true -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { askPassword = "codes" }, modifier = Modifier.weight(1f)) { Text("New recovery codes") }
                TextButton(onClick = { askPassword = "off" }) { Text("Turn off", color = MaterialTheme.colorScheme.error) }
            }
            null -> Unit
        }
    }

    askPassword?.let { purpose ->
        PasswordDialog(
            title = when (purpose) { "on" -> "Set up two-step verification"; "codes" -> "New recovery codes"; else -> "Turn off two-step verification" },
            message = when (purpose) {
                "on" -> "Confirm your password to continue."
                "codes" -> "Your current recovery codes will stop working."
                else -> "Anyone with your password could then sign in. Not recommended."
            },
            busy = runner.busy,
            onDismiss = { askPassword = null },
            onConfirm = { password ->
                runner.run(if (purpose == "off") "Two-step verification is off" else null) {
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
                    runner.toast("Install Google Authenticator (or a similar app) first, or enter the key manually")
                }
            },
            onConfirm = { code ->
                runner.run("Two-step verification is on") {
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
        title = { Text("Set up two-step verification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1. Add Stock Anchor to Google Authenticator, Microsoft Authenticator or a similar app:", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onOpenApp, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("Open authenticator app")
                }
                Text("Or enter this key in the app (time-based):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(setup.secret.chunked(4).joinToString(" "), fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    TextButton(onClick = { clipboard.setText(AnnotatedString(setup.secret)) }) {
                        Icon(Icons.Filled.ContentCopy, null); Spacer(Modifier.width(4.dp)); Text("Copy")
                    }
                }
                Text("2. Enter the 6-digit code the app shows:", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("Code") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(code) }, enabled = !busy && code.length == 6) { Text("Turn on") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RecoveryCodesDialog(codes: List<String>, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Save your recovery codes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("If you lose your phone, each code signs you in once. They are shown only now — keep them somewhere safe.",
                    style = MaterialTheme.typography.bodyMedium)
                codes.chunked(2).forEach { pair ->
                    Row { pair.forEach { Text(it, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)) } }
                }
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(codes.joinToString("\n"))); copied = true }) {
                    Icon(Icons.Filled.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text(if (copied) "Copied" else "Copy codes")
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("I saved them") } },
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
                    password, { password = it }, label = { Text("Your password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(password) }, enabled = !busy && password.isNotEmpty()) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
