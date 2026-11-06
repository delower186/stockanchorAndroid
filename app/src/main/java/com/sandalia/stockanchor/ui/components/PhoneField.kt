package com.sandalia.stockanchor.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import com.sandalia.stockanchor.R
import com.sandalia.stockanchor.util.str

/**
 * Phone number input. Tapping it while empty offers "choose from contacts" or "type the number"; the icon opens the
 * contact list at any time. The system contact picker is used, so the app needs no contacts permission and only
 * receives the one number the person picks.
 */
@Composable
fun PhoneField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    val interaction = remember { MutableInteractionSource() }
    var menuOpen by remember { mutableStateOf(false) }
    var typing by remember { mutableStateOf(false) }
    val isEmpty by rememberUpdatedState(value.isEmpty())
    val latestOnChange by rememberUpdatedState(onValueChange)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri -> phoneNumberAt(context, uri)?.let { latestOnChange(it) } }
    }
    val openContacts = {
        runCatching { picker.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)) }
            .onFailure { Toast.makeText(context, str(R.string.no_contacts_app_found), Toast.LENGTH_LONG).show() }
        Unit
    }

    LaunchedEffect(interaction) {
        interaction.interactions.collect { if (it is PressInteraction.Release && isEmpty && !typing) menuOpen = true }
    }

    Box(modifier) {
        OutlinedTextField(
            value, onValueChange,
            label = { Text(str(R.string.phone)) },
            singleLine = true,
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            trailingIcon = { IconButton(onClick = openContacts) { Icon(Icons.Filled.Contacts, str(R.string.choose_from_contacts)) } },
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
        DropdownMenu(menuOpen, { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(str(R.string.choose_from_contacts)) },
                leadingIcon = { Icon(Icons.Filled.Contacts, null) },
                onClick = { menuOpen = false; openContacts() },
            )
            DropdownMenuItem(
                text = { Text(str(R.string.type_the_number)) },
                leadingIcon = { Icon(Icons.Filled.Dialpad, null) },
                onClick = {
                    menuOpen = false
                    typing = true
                    focus.requestFocus()
                    keyboard?.show()
                },
            )
        }
    }
}

private fun phoneNumberAt(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0)?.trim() else null
    }
}.getOrNull()
