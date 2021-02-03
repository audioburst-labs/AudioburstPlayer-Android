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
The library requires an application key which can be obtained via [Audioburst Publishers](https://publishers.audioburst.com/). An experience ID is also available. This is a unique identifier for the customized playlist topics chosen during the setup process.

## Add AudioburstPlayer to your app

### Step 1. Add AudioburstPlayer dependency
[ ![Download Android](https://api.bintray.com/packages/audioburst/maven/audioburst_player/images/download.svg) ](https://bintray.com/audioburst/maven/audioburst_player/_latestVersion)

Add AudioburstPlayer Android SDK to your project. To do this, add the following dependency in your app level `build.gradle` file:
```gradle
implementation 'com.audioburst:audioburst_player:{latest-version}'
```

In case you're getting a "Duplicate class" on Kotlin Coroutines dependencies, you need to exclude those from AudioburstPlayer library in the following way:
```gradle
implementation ("com.audioburst:audioburst_player:{latest-version}") {
    exclude group: "org.jetbrains.kotlinx", module: "kotlinx-coroutines-core-jvm"
}
```

The library is built using the Kotlin language and is using `Coroutines`, so to be able to support it you need to add the following configurations to your `android` script in the app level `build.config` file:
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

### Step 2. Init AudioburstPlayer
AudioburstPlayer requires an application key to work. The player can be configured in two ways: via [Audioburst Publishers](https://publishers.audioburst.com/) after obtaining an experience ID or by passing a custom configuration.

#### Initialize AudioburstPlayer with application key and experience ID:
```kotlin
AudioburstPlayer.init(
    applicationKey = "YOUR_APP_KEY",
    experienceId = "YOUR_EXPERIENCE_ID"
)
```

#### Initialize AudioburstPlayer with application key and custom configuration:
```kotlin
AudioburstPlayer.init(
    configuration = AudioburstPlayer.Configuration(
        applicationKey = "YOUR_APP_KEY",
        action = ...,
        mode = ...,
        theme = ...,
        accentColor = ...,
        autoPlay = ...,
    ),
)
```

Parameters description:
- applicationKey - String - application key obtained from [Audioburst Publishers](https://publishers.audioburst.com/),
- action - Action enum - one of the types of playlists currently supported by the library,
- mode - Mode enum - mode in which you would like player to appear (Button or Banner),
- theme - Theme enum - theme of the players (Dark or Light),
- accentColor - String - color of accents in players. It needs to be a hex value that starts with `#` character,
- autoPlay - Boolean - whether player should start playing automatically after initialization or not.

Possible `action` values:
- AudioburstPlaylist(id: String)
- UserGeneratedPlaylist(id: String)
- PersonalPlaylist(id: String)
- SourcePlaylist(id: String)
- AccountPlaylist(id: String)
- Voice(byteArray: ByteArray)

Most of the options above accepts String `id` as a parameter. `Voice` playlist is a special type that accepts byte array from PCM file that should contain a voice saying what user would like to listen about.

### Step 3. Add `MiniPlayer` to your layout hierarchy
```xml
<fragment
    android:id="@+id/miniPlayer"
    android:name="com.audioburst.audioburst_player.MiniPlayer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

If you choose in Audioburst Publishers, to use the `Button Player` then this step is not required. The player will appear on the screen immediately after the `Experience` object is loaded.

You can also open `Full Player` on demand with the following function:
```kotlin
AudioburstPlayer.showFullPlayer(activity)
```

### Step 4. Play content on demand
Request the AudioburstPlayer to start playback at any time using this simple play() method:
```kotlin
AudioburstPlayer.play()
```
If AudioburstPlayer is not yet initialized this method call will cause the library to remember the request and playback will automatically start after the initialization process is completed.

### Step 5. Pass recorded PCM file
AudioburstPlayer is able to process raw audio files that contain a recorded request of what should be played. You can record a voice command stating what you would like to listen to and then upload it to your device and use AudioburstPlayer to play it.
```kotlin
AudioburstPlayer.loadPlaylist(byteArray)
```
The `loadPlaylist` function accepts `Byte Array` as an argument. A request included in the PCM file will be processed and the player will load a playlist of the bursts found. If no bursts are found, `ErrorListener` will be called.
Please remember that before playing any PCM file the SDK must be initialized.

### Step 6. Handle errors
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