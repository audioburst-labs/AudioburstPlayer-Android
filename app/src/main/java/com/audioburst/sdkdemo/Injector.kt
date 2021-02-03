package com.audioburst.sdkdemo

import android.content.Context
import com.audioburst.sdkdemo.data.CredentialPreferences
import kotlinx.serialization.json.Json

object Injector {

    fun provideHomeViewModel(context: Context): MainViewModel.Factory =
        MainViewModel.Factory(
            stringProvider = StringProvider(context),
            audioRecorder = AudioRecorder(context),
            converter = FileToByteArrayConverter(context),
            credentialPreferences = CredentialPreferences(context, Json {  })
        )
}