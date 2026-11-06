package com.sandalia.stockanchor.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Remembers whether a field has been used, so its error appears after typing in it or leaving it, not on an untouched form.
 */
@Composable
fun rememberFieldTouched(): FieldTouched {
    var touched by rememberSaveable { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    return remember {
        object : FieldTouched {
            override val value get() = touched
            override fun onEdit() { touched = true }
            override fun onFocus(focused: Boolean) {
                if (focused) hadFocus = true else if (hadFocus) touched = true
            }
        }
    }
}

interface FieldTouched {
    val value: Boolean
    fun onEdit()
    fun onFocus(focused: Boolean)
}

/**
 * Text field that stops at [maxLength] and shows [error] under it once the field has been used.
 * [counter] shows "12/30" where the limit is short enough to matter while typing.
 */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxLength: Int? = null,
    counter: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
) {
    val touched = rememberFieldTouched()
    // A value saved before the limit existed is flagged at once and can only get shorter, never cut off behind the person's back.
    val overLimit = maxLength != null && value.length > maxLength
    val shown = error.takeIf { touched.value || overLimit }
    OutlinedTextField(
        value = value,
        onValueChange = { typed ->
            touched.onEdit()
            when {
                maxLength == null || typed.length <= maxLength || typed.length < value.length -> onValueChange(typed)
                !overLimit -> onValueChange(typed.take(maxLength))
            }
        },
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        isError = shown != null,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        supportingText = when {
            shown != null -> { { Text(shown) } }
            hint != null -> { { Text(hint) } }
            supporting != null -> supporting
            else -> null
        },
        suffix = if (counter && maxLength != null) { { Text("${value.length}/$maxLength") } } else null,
        modifier = modifier.onFocusChanged { touched.onFocus(it.isFocused) },
    )
}
