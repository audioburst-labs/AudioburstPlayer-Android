package com.audioburst.sdkdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.audioburst.audioburst_player.AudioburstPlayer
import com.audioburst.sdkdemo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val pagerAdapter by lazy { MainPagerAdapter(this) }
    private val viewModel by viewModels<MainViewModel> { Injector.provideHomeViewModel(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.render()
    }

    private fun ActivityMainBinding.render() {
        viewModel.tabView
            .onEach { tabLayout.render(it) }
            .launchIn(lifecycleScope)

        viewModel.sdkKeysView
            .onEach { pagerAdapter.render(it) }
            .launchIn(lifecycleScope)

        viewModel.customParamsView
            .onEach { pagerAdapter.render(it) }
            .launchIn(lifecycleScope)

        viewModel.currentTab
            .onEach { viewPager.setCurrentItem(it, true) }
            .launchIn(lifecycleScope)

        viewModel.isRecordingOverlayVisible
            .onEach { recordingOverlayView.isVisible = it }
            .launchIn(lifecycleScope)

        viewModel.events
            .onEach { it.handle() }
            .launchIn(lifecycleScope)

        viewPager.adapter = pagerAdapter

        stopRecordingButton.setOnClickListener { viewModel.stopRecording() }

        mainWelcomeTextView.setOnClickListener { AudioburstPlayer.showFullPlayer(this@MainActivity) }
    }

    private fun Event.handle() {
        when (this) {
            Event.PermissionRequest -> checkAudioPermission()
            is Event.ErrorMessage -> display()
            is Event.SdkKeysInitialization -> {
                AudioburstPlayer.init(
                    applicationKey = applicationKey,
                    experienceId = experienceId,
                )
            }
            is Event.ConfigurationInitialization -> {
                AudioburstPlayer.init(configuration)
            }
        }.exhaustive
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.text?.let { viewModel.onTabSelected(it.toString()) }
        }
    }

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
        override fun onPageSelected(position: Int) {
            viewModel.onPageSelected(position)
        }
    }

    private val errorListener: AudioburstPlayer.ErrorListener = AudioburstPlayer.ErrorListener {
        viewModel.onSdkError(it)
    }

    override fun onResume() {
        super.onResume()
        AudioburstPlayer.addErrorListener(errorListener)
        with(binding) {
            tabLayout.addOnTabSelectedListener(tabSelectedListener)
            viewPager.addOnPageChangeListener(pageChangeListener)
        }
    }

    override fun onStop() {
        super.onStop()
        AudioburstPlayer.removeErrorListener(errorListener)
        viewModel.stopRecording()
        with(binding) {
            tabLayout.removeOnTabSelectedListener(tabSelectedListener)
            viewPager.removeOnPageChangeListener(pageChangeListener)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        }
    }

    private fun checkAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, permission) -> viewModel.onPermissionGranted()
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun Event.ErrorMessage.display() {
        Snackbar.make(binding.root, message, 5000).show()
    }
}
