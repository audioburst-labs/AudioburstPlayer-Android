package com.audioburst.sdkdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audioburst.audioburst_player.AudioburstPlayer
import com.audioburst.audioburst_player.AudioburstPlayer.Configuration
import com.audioburst.audioburst_player.AudioburstPlayer.Configuration.*
import com.audioburst.sdkdemo.custom_views.CustomParamsView
import com.audioburst.sdkdemo.custom_views.CustomParamsView.Event.*
import com.audioburst.sdkdemo.custom_views.CustomParamsView.Mode.Banner
import com.audioburst.sdkdemo.custom_views.CustomParamsView.Mode.Button
import com.audioburst.sdkdemo.custom_views.CustomParamsView.Theme.Dark
import com.audioburst.sdkdemo.custom_views.CustomParamsView.Theme.Light
import com.audioburst.sdkdemo.custom_views.PlaylistConfigurationView
import com.audioburst.sdkdemo.custom_views.SdkKeysView
import com.audioburst.sdkdemo.custom_views.TabView
import com.audioburst.sdkdemo.data.CredentialPreferences
import com.audioburst.sdkdemo.data.CustomParams
import com.audioburst.sdkdemo.data.SdkKeys
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val stringProvider: StringProvider,
    private val audioRecorder: AudioRecorder,
    private val converter: FileToByteArrayConverter,
    private val credentialPreferences: CredentialPreferences,
) : ViewModel() {

    private var currentRecording: ByteArray? = null

    private val _tabView = MutableStateFlow(
        TabView.Configuration(listOf(stringProvider.getString(R.string.main_tab_credentials), stringProvider.getString(R.string.main_tab_custom_parmas), stringProvider.getString(R.string.main_tab_playlist_configuration)))
    )
    val tabView = _tabView.asStateFlow()

    private val _playlistConfiguration = MutableStateFlow(PlaylistConfigurationView.Configuration(onEvent = ::onEvent))
    val playlistConfiguration = _playlistConfiguration.asStateFlow()

    private val _sdkKeysView = MutableStateFlow(SdkKeysView.Configuration(onReloadButtonClicked = ::onReloadButton))
    val sdkKeysView = _sdkKeysView.asStateFlow()

    private val _customParamsView = MutableStateFlow(CustomParamsView.Configuration(
        onEvent = ::onEvent,
        actionTypes = PlayerAction.values().map { CustomParamsView.ActionType(it, isSelected = false) }
    ))
    val customParamsView = _customParamsView.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab = _currentTab.asStateFlow()

    private val _isRecordingOverlayVisible = MutableStateFlow(false)
    val isRecordingOverlayVisible = _isRecordingOverlayVisible.asStateFlow()

    private val _events: MutableSharedFlow<Event> = MutableSharedFlow()
    val events: Flow<Event> = _events.asSharedFlow()

    init {
        credentialPreferences
            .sdkKeysFlow
            .filterNotNull()
            .map { it.toSdkKeysViewConfiguration(_sdkKeysView.value) }
            .onEach { _sdkKeysView.value = it }
            .launchIn(viewModelScope)

        credentialPreferences
            .customParamsFlow
            .filterNotNull()
            .map { it.toCustomParamsViewConfiguration(_customParamsView.value) }
            .onEach { _customParamsView.value = it }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tabTitle: String) {
        _currentTab.value = _tabView.value.tabTitles.indexOfFirst { it == tabTitle }
    }

    fun onPageSelected(position: Int) {
        _tabView.value = _tabView.value.copy(selectedIndex = position)
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _isRecordingOverlayVisible.value = true
            when (val result = audioRecorder.record()) {
                is AudioRecorder.Result.Success -> currentRecording = converter.convert(result.output)
                is AudioRecorder.Result.Failure -> _events.emit(Event.ErrorMessage(stringProvider.getString(R.string.main_recording_error)))
            }
            _isRecordingOverlayVisible.value = false
        }
    }

    fun stopRecording() {
        if (audioRecorder.isRecording) {
            audioRecorder.stopRecording()
        }
    }

    fun onSdkError(it: AudioburstPlayer.Error) {
        viewModelScope.launch {
            _events.emit(Event.ErrorMessage(it.message()))
        }
    }

    private fun onReloadButton(applicationKey: String, experienceId: String) {
        credentialPreferences.sdkKeys = SdkKeys(applicationKey, experienceId)
        viewModelScope.launch {
            _events.emit(Event.SdkKeysInitialization(applicationKey, experienceId))
        }
    }

    private fun buildConfiguration(applicationKey: String, actionValue: String, colorAccent: String) {
        val configuration = _customParamsView.value
        val action = configuration.actionTypes.first { it.isSelected }.playerAction.action(
            id = actionValue,
            byteArray = currentRecording ?: byteArrayOf()
        )

        if (action is Action.Voice && action.byteArray.isEmpty()) {
            viewModelScope.launch {
                _events.emit(Event.ErrorMessage(
                    message = stringProvider.getString(R.string.main_no_recording_error, stringProvider.getString(R.string.main_playlist_voice)))
                )
            }
            return
        }

        val defaultValues = Configuration(applicationKey, action)
        val sdkConfiguration = Configuration(
            applicationKey = applicationKey,
            action = action,
            mode = when (configuration.mode) {
                Banner -> Mode.Banner
                Button -> Mode.Button
                null -> defaultValues.mode
            },
            theme = when (configuration.theme) {
                Light -> Theme.Light
                Dark -> Theme.Dark
                null -> defaultValues.theme
            },
            accentColor = colorAccent,
            autoPlay = configuration.autoPlay ?: defaultValues.autoPlay,
        )
        credentialPreferences.customParams = CustomParams(
            applicationKey = applicationKey,
            selectedPlayerAction = configuration.actionTypes.first { it.isSelected }.playerAction,
            actionValue = actionValue,
            mode = configuration.mode,
            theme = configuration.theme,
            colorAccent = colorAccent,
            autoPlay = configuration.autoPlay,
        )
        viewModelScope.launch {
            _events.emit(Event.ConfigurationInitialization(sdkConfiguration))
        }
    }

    private fun onEvent(event: CustomParamsView.Event) {
        _customParamsView.value = when (event) {
            is ActionTypeChanged -> event.handle()
            is PlayerModeClicked -> _customParamsView.value.copy(mode = event.mode)
            is PlayerThemeClicked -> _customParamsView.value.copy(theme = event.theme)
            is AutoPlayClicked -> _customParamsView.value.copy(autoPlay = _customParamsView.value.autoPlay?.let { !it } ?: !event.isSelected)
            is ReloadButtonClicked -> event.handle()
            is RecordButtonClicked -> event.handle()
            is OnTextChanged.ApplicationKey -> _customParamsView.value.copy(applicationKey = _customParamsView.value.applicationKey.copy(text = event.value, updateText = false))
            is OnTextChanged.ActionValue -> _customParamsView.value.copy(actionValue = _customParamsView.value.actionValue.copy(text = event.value, updateText = false))
            is OnTextChanged.AccentColor -> _customParamsView.value.copy(colorAccent = _customParamsView.value.colorAccent.copy(text = event.value, updateText = false))
        }
    }

    private fun ActionTypeChanged.handle(): CustomParamsView.Configuration {
        if (playerAction != PlayerAction.Voice) {
            currentRecording = null
        }

        return _customParamsView.value.copy(
            actionTypes = _customParamsView.value.actionTypes.map {
                it.copy(isSelected = it.playerAction == playerAction)
            },
            actionValue = _customParamsView.value.actionValue.copy(isEnabled = playerAction != PlayerAction.Voice),
            isRecordButtonEnabled = playerAction == PlayerAction.Voice
        )
    }

    private fun ReloadButtonClicked.handle(): CustomParamsView.Configuration {
        val applicationKeyError = if (applicationKey.isEmpty()) stringProvider.getString(R.string.custom_params_app_key_error) else null
        val actionValueError = if (_customParamsView.value.actionTypes.firstOrNull { it.isSelected }?.playerAction != PlayerAction.Voice && actionValue.isEmpty())
            stringProvider.getString(R.string.custom_params_player_action_value_error) else null

        if (applicationKeyError == null && actionValueError == null) {
            buildConfiguration(
                applicationKey = applicationKey,
                actionValue = actionValue,
                colorAccent = colorAccent,
            )
        }

        return _customParamsView.value.copy(
            applicationKey = _customParamsView.value.applicationKey.copy(errorText = applicationKeyError),
            actionValue = _customParamsView.value.actionValue.copy(errorText = actionValueError)
        )
    }

    private fun RecordButtonClicked.handle(): CustomParamsView.Configuration =
        _customParamsView.value.copy(
            applicationKey = _customParamsView.value.applicationKey.copy(errorText = null),
            actionValue = _customParamsView.value.actionValue.copy(errorText = null),
            colorAccent = _customParamsView.value.colorAccent.copy(errorText = null),
        ).apply {
            viewModelScope.launch {
                _events.emit(Event.PermissionRequest)
            }
        }

    private fun AudioburstPlayer.Error.message(): String =
        stringProvider.getString(
            when (this) {
                AudioburstPlayer.Error.Network -> R.string.error_network
                AudioburstPlayer.Error.Server -> R.string.error_server
                AudioburstPlayer.Error.Unexpected -> R.string.error_unexpected
                AudioburstPlayer.Error.WrongApplicationKey -> R.string.error_wrong_app_key
                AudioburstPlayer.Error.WrongExperienceId -> R.string.error_wrong_experience_id
                AudioburstPlayer.Error.NoBursts -> R.string.error_no_bursts
            }
        )

    private fun onEvent(event: PlaylistConfigurationView.Event) {
        _playlistConfiguration.value = when (event) {
            is PlaylistConfigurationView.Event.ShowToolbarClicked -> _playlistConfiguration.value.copy(showToolbar = !_playlistConfiguration.value.showToolbar)
            is PlaylistConfigurationView.Event.OnTextChanged.OptOutSectionIds -> _playlistConfiguration.value.copy(optOutSectionIdsText = _playlistConfiguration.value.optOutSectionIdsText.copy(text = event.value, updateText = false))
            is PlaylistConfigurationView.Event.OnTextChanged.ToolbarTitle -> _playlistConfiguration.value.copy(toolbarTitleText = _playlistConfiguration.value.toolbarTitleText.copy(text = event.value, updateText = false))
            is PlaylistConfigurationView.Event.ShowPlaylistView -> handleShowPlaylistView()
            is PlaylistConfigurationView.Event.SectionTypeRadioButtonClicked -> _playlistConfiguration.value.copy(sectionType = when (_playlistConfiguration.value.sectionType) {
                PlaylistConfigurationView.SectionType.Horizontal -> PlaylistConfigurationView.SectionType.Grid
                PlaylistConfigurationView.SectionType.Grid -> PlaylistConfigurationView.SectionType.Horizontal
            })
            is PlaylistConfigurationView.Event.ShowMyPlaylistsClicked -> _playlistConfiguration.value.copy(showMyPlaylist = !_playlistConfiguration.value.showMyPlaylist)
            is PlaylistConfigurationView.Event.CloseOnPlaylistLoadClicked -> _playlistConfiguration.value.copy(closeOnPlaylistLoad = !_playlistConfiguration.value.closeOnPlaylistLoad)
        }
    }

    private fun handleShowPlaylistView(): PlaylistConfigurationView.Configuration {
        val configuration = AudioburstPlayer.PlaylistViewConfiguration(
            showToolbar = _playlistConfiguration.value.showToolbar,
            toolbarTitle = _playlistConfiguration.value.toolbarTitleText.text,
            sectionType = when (_playlistConfiguration.value.sectionType) {
                PlaylistConfigurationView.SectionType.Horizontal -> AudioburstPlayer.PlaylistViewConfiguration.SectionType.Horizontal
                PlaylistConfigurationView.SectionType.Grid -> AudioburstPlayer.PlaylistViewConfiguration.SectionType.Grid
            },
            showMyPlaylists = _playlistConfiguration.value.showMyPlaylist,
            closeOnPlaylistLoad = _playlistConfiguration.value.closeOnPlaylistLoad,
        )
        viewModelScope.launch {
            _events.emit(Event.ShowPlaylistView(configuration))
        }
        return _playlistConfiguration.value
    }

    class Factory(
        private val stringProvider: StringProvider,
        private val audioRecorder: AudioRecorder,
        private val converter: FileToByteArrayConverter,
        private val credentialPreferences: CredentialPreferences,
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(
                stringProvider = stringProvider,
                audioRecorder = audioRecorder,
                converter = converter,
                credentialPreferences = credentialPreferences,
            ) as T
        }
    }
}

private fun SdkKeys.toSdkKeysViewConfiguration(currentState: SdkKeysView.Configuration): SdkKeysView.Configuration =
    currentState.copy(
        applicationKey = currentState.applicationKey.copy(text = applicationKey, updateText = true),
        experienceId = currentState.experienceId.copy(text = experienceId, updateText = true),
    )

private fun CustomParams.toCustomParamsViewConfiguration(currentState: CustomParamsView.Configuration): CustomParamsView.Configuration =
    currentState.copy(
        applicationKey = currentState.applicationKey.copy(text = applicationKey, updateText = true),
        actionTypes = currentState.actionTypes.map {
            it.copy(isSelected = it.playerAction == selectedPlayerAction)
        },
        actionValue = currentState.actionValue.copy(text = actionValue, updateText = true),
        mode = mode,
        theme = theme,
        colorAccent = currentState.colorAccent.copy(text = colorAccent, updateText = true),
        autoPlay = autoPlay,
    )