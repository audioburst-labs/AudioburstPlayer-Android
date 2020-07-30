# AudioburstPlayer Android SDK

## Introduction
AudioburstPlayer is the SDK for Android that plays a pre-arranged playlist of audio items - or ‘bursts’ - short snippets of spoken-word audio sourced from live radio and premium podcasts.

## Features
AudioburstPlayer offers two modes: compact (mini or floating) and full screen. Both offer the following features:
- Play any burst from the playlist
- Skip to the next or previous burst
- Keep listening (switch to a longer version of the burst)
- Move playhead backward and forward within a single burst
- Displays Burst title and Show name
- Playlist plays continuously in background
- Playlist can be controlled from locked screen
- Play playlist via alternative audio output: headphones, bluetooth devices or AirPlay
- View/scroll bursts in playlists
- Includes support for Dark Mode

<p align="middle">
<img src="screenshots/1.png?raw=true"  width="200" hspace="5" title="Floating player"/><img src="screenshots/2.png?raw=true"  width="200" hspace="5" /><img src="screenshots/3.png?raw=true"  width="200" hspace="5" /><img src="screenshots/4.png?raw=true"  width="200" />
</p>

## Requirements
- Android 5.1+

## Get Started

This guide is a quick walkthrough to add AudioburstPlayer to an Android app. We recommend Android Studio as the development environment for building an app with the AudioburstPlayer SDK. The AudioburstPlayer-Android application showcases all features of the AudioburstPlayer.

## Prerequisites

### Audioburst API key
The application requires an application key and experience ID, both of which can be obtained via [Audioburst Publishers](https://studio.audioburst.com/). The experience ID is a unique identifier for the customized playlist topics chosen during the setup process in Audioburst Publishers.

## Add AudioburstPlayer to your app

### Step 1. Add AudioburstPlayer dependency
[ ![Download Android](https://api.bintray.com/packages/audioburst/maven/audioburst_player/images/download.svg) ](https://bintray.com/audioburst/maven/audioburst_player/_latestVersion)

Add AudioburstPlayer Android SDK to your project. To do this, add the following dependency in your app level `build.gradle` file:
```gradle
implementation 'com.audioburst:audioburst_player:{latest-version}'
```

Library is built in Kotlin language and is using `Coroutines`, so to be able to support it you need to add following configurations to your `android` script in app level `build.config` file:
```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude 'META-INF/AL2.0'
        exclude 'META-INF/LGPL2.1'
    }
}
```

### Step 2. Add `MiniPlayer` to your layout hierarchy
```xml
<fragment
    android:id="@+id/miniPlayer"
    android:name="com.audioburst.audioburst_player.MiniPlayer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

If you decide in Audioburst Studio, that you want to use `FloatingPlayer` then you don't need this step. Player will appear on the screen right after `Experience` object is loaded.

You can also open `Full Player` on demand with the following function:
```kotlin
AudioburstPlayer.showFullPlayer(this)
```
Please remember to first initialize the library as it is described in next step.

### Step 3. Init AudioburstPlayer
Initialize AudioburstPlayer in your `Application.onCreate` method:
```kotlin
override fun onCreate() {
    super.onCreate()
    AudioburstPlayer.init(
        context = this,
        sdkKeys = SdkKeys(
            applicationKey = "YOUR_APP_KEY",
            experienceId = "YOUR_EXPERIENCE_ID"
        )
    )
} 
```

### Step 4. Start playing Audioburst content:
You simply need to call one method to start playing Audioburst content:
```kotlin
AudioburstPlayer.startPlaying()
```

in case you would like to change keys you used to initialize a SDK, you can also call:
```kotlin
AudioburstPlayer.setKeys(
    sdkKeys = SdkKeys(
        applicationKey = "YOUR_APP_KEY",
        experienceId = "YOUR_EXPERIENCE_ID"
    )
)
```

### Step 5. Handle errors
In the event of an error when communicating with the API, we provide a way to monitor those events:
```kotlin
class MainActivity : AppCompatActivity(R.layout.activity_main), AudioburstPlayer.ErrorListener {
    override fun onError(error: AudioburstPlayer.Error) {
        // Handle error here
    }
}
```
And add listener:
```kotlin
AudioburstPlayer.addErrorListener(this)
```
Don’t forget to unregister `ErrorListener` to avoid memory leaks:
```kotlin
AudioburstPlayer.removeErrorListener(this)
```

## Dependencies
- Kotlin
- AppCompat
- Ktx
- Coroutines
- ConstraintLayout
- Dagger
- SwipeLayout
- Lottie
- Retrofit
- ThreeTenBp
- ExoPlayer
- Groupie
- Glide

## Privacy Policy
[Privacy Policy](https://audioburst.com/privacy)

## Terms of Service
[Terms of Service](https://audioburst.com/audioburst-publisher-terms)