package com.audioburst.sdkdemo

import android.app.Application
import com.audioburst.audioburst_player.AudioburstPlayer
import com.audioburst.audioburst_player.SdkKeys

class AudioburstPlayerDemo : Application() {

    override fun onCreate() {
        super.onCreate()
        AudioburstPlayer.init(
            context = this,
            sdkKeys = SdkKeys(
                applicationKey = BuildConfig.APPLICATION_KEY,
                experienceId = BuildConfig.EXPERICE_ID
            )
        )
    }
}
