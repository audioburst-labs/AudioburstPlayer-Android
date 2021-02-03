package com.audioburst.sdkdemo.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.audioburst.sdkdemo.R
import com.audioburst.sdkdemo.databinding.ViewSdkKeysBinding

class SdkKeysView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by lazy { ViewSdkKeysBinding.bind(this) }
    private var configuration: Configuration? = null

    init {
        View.inflate(context, R.layout.view_sdk_keys, this)

        with(binding) {
            reloadButton.setOnClickListener {
                configuration?.let {
                    it.onReloadButtonClicked(
                        applicationKeyEditText.text.toString(),
                        experienceIdEditView.text.toString(),
                    )
                }
            }
        }
    }

    fun render(configuration: Configuration) {
        this.configuration = configuration
        with(binding) {
            applicationKeyEditText.render(configuration.applicationKey)
            experienceIdEditView.render(configuration.experienceId)
        }
    }

    data class Configuration(
        val applicationKey: EditTextState = EditTextState(),
        val experienceId: EditTextState = EditTextState(),
        val onReloadButtonClicked: (applicationKey: String, experienceId: String) -> Unit,
    )
}