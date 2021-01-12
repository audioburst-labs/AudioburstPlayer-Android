package com.audioburst.sdkdemo

import android.app.Application
import com.audioburst.audioburst_player.AudioburstPlayer

class AudioburstPlayerDemo : Application() {

    override fun onCreate() {
        super.onCreate()
        AudioburstPlayer.init(
            applicationKey = BuildConfig.APPLICATION_KEY,
            experienceId = BuildConfig.EXPERICE_ID
        )
    }
}
