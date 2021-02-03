package com.audioburst.sdkdemo

import androidx.annotation.StringRes
import com.audioburst.audioburst_player.AudioburstPlayer

enum class PlayerAction(@StringRes val actionName: Int) {
    AudioburstPlaylist(R.string.main_playlist_audioburst),
    UserGeneratedPlaylist(R.string.main_playlist_user_generated),
    PersonalPlaylist(R.string.main_playlist_personal),
    SourcePlaylist(R.string.main_playlist_source),
    AccountPlaylist(R.string.main_playlist_account),
    Voice(R.string.main_playlist_voice),
}

fun PlayerAction.action(id: String, byteArray: ByteArray): AudioburstPlayer.Configuration.Action =
    when (this) {
        PlayerAction.AudioburstPlaylist -> AudioburstPlayer.Configuration.Action.AudioburstPlaylist(id)
        PlayerAction.UserGeneratedPlaylist -> AudioburstPlayer.Configuration.Action.UserGeneratedPlaylist(id)
        PlayerAction.PersonalPlaylist -> AudioburstPlayer.Configuration.Action.PersonalPlaylist(id)
        PlayerAction.SourcePlaylist -> AudioburstPlayer.Configuration.Action.SourcePlaylist(id)
        PlayerAction.AccountPlaylist -> AudioburstPlayer.Configuration.Action.AccountPlaylist(id)
        PlayerAction.Voice -> AudioburstPlayer.Configuration.Action.Voice(byteArray)
    }