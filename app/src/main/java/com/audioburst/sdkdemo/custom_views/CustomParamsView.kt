package com.audioburst.sdkdemo.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ScrollView
import androidx.annotation.IdRes
import com.audioburst.sdkdemo.PlayerAction
import com.audioburst.sdkdemo.R
import com.audioburst.sdkdemo.databinding.ViewCustomParamsBinding
import com.audioburst.sdkdemo.onTextChanged

class CustomParamsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private val binding by lazy { ViewCustomParamsBinding.bind(this) }
    private var configuration: Configuration? = null
    private val adapter by lazy {
        ArrayAdapter(context, android.R.layout.simple_spinner_item, mutableListOf<String>()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    init {
        View.inflate(context, R.layout.view_custom_params, this)

        with(binding) {
            playlistTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    configuration?.actionTypes?.getOrNull(position)?.let {
                        Event.ActionTypeChanged(it.playerAction)
                    }?.let(this@CustomParamsView::onEvent)
                }
            }
            playlistRecordButton.setOnClickListener {
                onEvent(Event.RecordButtonClicked)
            }
            playerModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                Mode.values().firstOrNull { it.resId == checkedId }?.let {
                    onEvent(Event.PlayerModeClicked(it))
                }
            }
            playerThemeRadioButton.setOnCheckedChangeListener { _, checkedId ->
                Theme.values().firstOrNull { it.resId == checkedId }?.let {
                    onEvent(Event.PlayerThemeClicked(it))
                }
            }
            switchContainer.setOnClickListener {
                onEvent(Event.AutoPlayClicked(autoplaySwitch.isSelected))
            }
            reloadButton.setOnClickListener {
                onEvent(
                    Event.ReloadButtonClicked(
                        applicationKey = applicationKeyEditText.text.toString(),
                        actionValue = playlistValueEditText.text.toString(),
                        colorAccent = colorAccentEditText.text.toString(),
                    )
                )
            }
            applicationKeyEditText.observeTextChanged(Event.OnTextChanged::ApplicationKey)
            playlistValueEditText.observeTextChanged(Event.OnTextChanged::ActionValue)
            colorAccentEditText.observeTextChanged(Event.OnTextChanged::AccentColor)
            playlistTypeSpinner.adapter = adapter
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
            applicationKeyEditText.render(configuration.applicationKey)

            var selectedIndex: Int? = null
            adapter.clear()
            configuration.actionTypes.forEachIndexed { index, action ->
                adapter.add(context.getString(action.playerAction.actionName))
                if (action.isSelected) {
                    selectedIndex = index
                }
            }
            selectedIndex?.let(playlistTypeSpinner::setSelection)
            adapter.notifyDataSetChanged()

            playlistValueEditText.render(configuration.actionValue)

            playlistRecordButton.isEnabled = configuration.isRecordButtonEnabled

            configuration.mode?.let { playerModeRadioGroup.check(it.resId) }

            configuration.theme?.let { playerThemeRadioButton.check(it.resId) }

            colorAccentEditText.render(configuration.colorAccent)

            configuration.autoPlay?.let { autoplaySwitch.isChecked = it }
        }
    }

    data class Configuration(
        val applicationKey: EditTextState = EditTextState(),
        val actionTypes: List<ActionType> = emptyList(),
        val actionValue: EditTextState = EditTextState(),
        val isRecordButtonEnabled: Boolean = false,
        val mode: Mode? = null,
        val theme: Theme? = null,
        val colorAccent: EditTextState = EditTextState(),
        val autoPlay: Boolean? = null,
        val onEvent: (Event) -> Unit,
    )

    data class ActionType(
        val playerAction: PlayerAction,
        val isSelected: Boolean = false,
    )

    enum class Mode(@IdRes val resId: Int) {
        Banner(R.id.bannerRadioButton), Button(R.id.buttonRadioButton)
    }

    enum class Theme(@IdRes val resId: Int) {
        Light(R.id.lightModeRadioButton), Dark(R.id.darkModeRadioButton)
    }

    sealed class Event {
        data class ActionTypeChanged(val playerAction: PlayerAction) : Event()
        data class PlayerModeClicked(val mode: Mode) : Event()
        data class PlayerThemeClicked(val theme: Theme) : Event()
        data class AutoPlayClicked(val isSelected: Boolean) : Event()
        data class ReloadButtonClicked(val applicationKey: String, val actionValue: String, val colorAccent: String): Event()
        sealed class OnTextChanged : Event() {
            data class ApplicationKey(val value: String): OnTextChanged()
            data class ActionValue(val value: String): OnTextChanged()
            data class AccentColor(val value: String): OnTextChanged()
        }
        object RecordButtonClicked: Event()
    }
}