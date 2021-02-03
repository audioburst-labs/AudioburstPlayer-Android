package com.audioburst.sdkdemo

import com.audioburst.sdkdemo.custom_views.CustomParamsView
import com.audioburst.sdkdemo.custom_views.SdkKeysView

data class MainViewState(
    val sdkKeysViewConfiguration: SdkKeysView.Configuration,
    val customParamsViewConfiguration: CustomParamsView.Configuration,
    val tabs: List<TabItem>,
    val currentTab: Int,
    val isRecordingOverlayVisible: Boolean,
)