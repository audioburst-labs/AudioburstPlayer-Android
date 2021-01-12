package com.audioburst.sdkdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.audioburst.audioburst_player.AudioburstPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main), AudioburstPlayer.ErrorListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainApplicationKeyEditText.setText(AudioburstPlayer.applicationKey)
        mainExperienceIdEditView.setText(AudioburstPlayer.experienceId)

        mainReloadButton.setOnClickListener {
            AudioburstPlayer.init(
                applicationKey = mainApplicationKeyEditText.text.toString(),
                experienceId = mainExperienceIdEditView.text.toString()
            )
        }

        mainWelcomeTextView.setOnClickListener {
            AudioburstPlayer.showFullPlayer(this)
        }
    }

    override fun onResume() {
        super.onResume()
        AudioburstPlayer.addErrorListener(this)
    }

    override fun onStop() {
        super.onStop()
        AudioburstPlayer.removeErrorListener(this)
    }

    override fun onError(error: AudioburstPlayer.Error) {
        Log.i("MainActivity", error.name)
    }
}
