package com.audioburst.sdkdemo

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

@Suppress("unused")
inline val Any?.exhaustive get() = Unit

inline fun EditText.onTextChanged(crossinline onChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { onChanged(it.toString()) }
        }
    })
}