package com.audioburst.sdkdemo

import com.audioburst.audioburst_player.AudioburstPlayer

sealed class Event {
    object PermissionRequest : Event()

    class ErrorMessage(val message: String) : Event()

    data class SdkKeysInitialization(
        val applicationKey: String,
        val experienceId: String,
    ) : Event()

    data class ConfigurationInitialization(val configuration: AudioburstPlayer.Configuration) : Event()

    data class ShowPlaylistView(val configuration: AudioburstPlayer.PlaylistViewConfiguration) : Event()

    object LoadPlaylist : Event()
}