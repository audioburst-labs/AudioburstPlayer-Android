package com.audioburst.sdkdemo

import android.content.Context
import androidx.annotation.StringRes

class StringProvider(private val context: Context) {
    fun getString(@StringRes stringRes: Int): String = context.getString(stringRes)

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String = context.getString(resId, *formatArgs)
}