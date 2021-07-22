package com.audioburst.sdkdemo.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.audioburst.sdkdemo.PlayerAction
import com.audioburst.sdkdemo.custom_views.CustomParamsView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CredentialPreferences(context: Context, private val json: Json) {

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var sdkKeys: SdkKeys?
        get() = sharedPreferences.getString(SDK_KEYS_KEY, null)?.let { json.decodeFromString(it) }
        set(value) {
            value?.let {
                sharedPreferences.edit {
                    putString(SDK_KEYS_KEY, json.encodeToString(it))
                }
            }
        }

    val sdkKeysFlow: Flow<SdkKeys?> = observe(SDK_KEYS_KEY) { sdkKeys }

    var customParams: CustomParams?
        get() = sharedPreferences.getString(CUSTOM_PARAMS_KEY, null)?.let { json.decodeFromString(it) }
        set(value) {
            value?.let {
                sharedPreferences.edit {
                    putString(CUSTOM_PARAMS_KEY, json.encodeToString(it))
                }
            }
        }

    val customParamsFlow: Flow<CustomParams?> = observe(CUSTOM_PARAMS_KEY) { customParams }

    private fun <T> observe(key: String, getter: () -> T): Flow<T> =
        callbackFlow {
            offer(getter())
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
                if (k == key) {
                    offer(getter())
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    companion object {
        private const val PREFERENCES_NAME = "com.audioburst.sdkdemo"
        private const val SDK_KEYS_KEY = "$PREFERENCES_NAME.SDK_KEYS"
        private const val CUSTOM_PARAMS_KEY = "$PREFERENCES_NAME.CUSTOM_PARAMS"
    }
}

@Serializable
data class SdkKeys(
    val applicationKey: String,
    val experienceId: String,
)

@Serializable
data class CustomParams(
    val applicationKey: String,
    val selectedPlayerAction: PlayerAction,
    val actionValue: String?,
    val mode: CustomParamsView.Mode?,
    val theme: CustomParamsView.Theme?,
    val colorAccent: String?,
    val autoPlay: Boolean = true,
)
