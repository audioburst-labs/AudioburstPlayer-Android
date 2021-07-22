package com.audioburst.sdkdemo.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import androidx.annotation.IdRes
import com.audioburst.sdkdemo.R
import com.audioburst.sdkdemo.databinding.ViewPlaylistConfigurationBinding
import com.audioburst.sdkdemo.onTextChanged

class PlaylistConfigurationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private val binding by lazy { ViewPlaylistConfigurationBinding.bind(this) }
    private var configuration: Configuration? = null

    init {
        View.inflate(context, R.layout.view_playlist_configuration, this)

        with(binding) {
            showButton.setOnClickListener {
                onEvent(Event.ShowPlaylistView)
            }
            switchContainer.setOnClickListener {
                onEvent(Event.ShowToolbarClicked(showToolbarSwitch.isSelected))
            }
            switchMyPlaylistContainer.setOnClickListener {
                onEvent(Event.ShowMyPlaylistsClicked(showMyPlaylistSwitch.isSelected))
            }
            switchCloseOnLoadContainer.setOnClickListener {
                onEvent(Event.CloseOnPlaylistLoadClicked(closeOnLoadSwitch.isSelected))
            }
            toolbarTitleEditText.observeTextChanged(Event.OnTextChanged::ToolbarTitle)
            sectionTypeRadioButton.setOnCheckedChangeListener { _, checkedId ->
                SectionType.values().firstOrNull { it.resId == checkedId }?.let {
                    onEvent(Event.SectionTypeRadioButtonClicked(it))
                }
            }
        }
    }

    private inline fun EditText.observeTextChanged(crossinline onTextChanged: (String) -> Event.OnTextChanged) {
        onTextChanged {
            onEvent(onTextChanged(it))
        }
    }

    private fun onEvent(event: Event) {
        configuration?.onEvent?.invoke(event)
    }

    fun render(configuration: Configuration) {
        this.configuration = configuration
        with(binding) {
            toolbarTitleEditText.render(configuration.toolbarTitleText)
            showToolbarSwitch.isChecked = configuration.showToolbar
            showMyPlaylistSwitch.isChecked = configuration.showMyPlaylist
            closeOnLoadSwitch.isChecked = configuration.closeOnPlaylistLoad
            sectionTypeRadioButton.check(configuration.sectionType.resId)
        }
    }

    data class Configuration(
        val optOutSectionIdsText: EditTextState = EditTextState(),
        val toolbarTitleText: EditTextState = EditTextState(text = "Podcast Bits"),
        val showToolbar: Boolean = true,
        val showMyPlaylist: Boolean = true,
        val closeOnPlaylistLoad: Boolean = true,
        val sectionType: SectionType = SectionType.Horizontal,
        val onEvent: (Event) -> Unit,
    )

    enum class SectionType(@IdRes val resId: Int) {
        Horizontal(R.id.horizontalRadioButton), Grid(R.id.gridRadioButton)
    }

    sealed class Event {
        data class ShowToolbarClicked(val isSelected: Boolean) : Event()
        data class ShowMyPlaylistsClicked(val isSelected: Boolean) : Event()
        data class CloseOnPlaylistLoadClicked(val isSelected: Boolean) : Event()
        data class SectionTypeRadioButtonClicked(val sectionType: SectionType) : Event()
        sealed class OnTextChanged : Event() {
            data class OptOutSectionIds(val value: String): OnTextChanged()
            data class ToolbarTitle(val value: String): OnTextChanged()
        }
        object ShowPlaylistView: Event()
    }
}