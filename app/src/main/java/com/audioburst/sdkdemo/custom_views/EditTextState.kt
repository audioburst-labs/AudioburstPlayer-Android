package com.audioburst.sdkdemo.custom_views

import android.widget.EditText

data class EditTextState(
    val text: String? = null,
    val updateText: Boolean = true,
    val errorText: String? = null,
    val isEnabled: Boolean = true,
)

fun EditText.render(editTextState: EditTextState) {
    if (editTextState.updateText) {
        setText(editTextState.text)
    }
    error = editTextState.errorText
    isEnabled = editTextState.isEnabled
}