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
[ ![Download Android](https://maven-badges.herokuapp.com/maven-central/com.audioburst/audioburst_player/badge.svg) ](https://search.maven.org/artifact/com.audioburst/audioburst_player)

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

If you choose in Audioburst Publishers or in the `Configuration` object, to use the `Button Player` then this step is not required.

### Step 4. Show chosen Player
AudioburstPlayer can inform you when the initialization process finishes and if it was successful. To do it, you need to pass `AudioburstPlayer.InitFinishListener` instance to the `init` method and get notified about it.
When the initialization process is successfully finished, you can show chosen Player on the user screen:
```kotlin
AudioburstPlayer.showPlayer()
```
This method will return true if it was possible to show the player. If returned value is equal to false, it means that AudioburstPlayer hasn't been initialized properly yet.

You can also open `Full Player` on demand with the following function:
```kotlin
AudioburstPlayer.showFullPlayer(activity)
```

### Step 5. Play/pause content on demand
Request the AudioburstPlayer to start/stop playback at any time using these simple methods:
```kotlin
AudioburstPlayer.play()
AudioburstPlayer.pause()
```
If AudioburstPlayer is not yet initialized `play()` method call will cause the library to remember the request and playback will automatically start after the initialization process is completed.

### Step 6. Pass recorded PCM file
AudioburstPlayer is able to process raw audio files that contain a recorded request of what should be played. You can record a voice command stating what you would like to listen to and then upload it to your device and use AudioburstPlayer to play it.
```kotlin
AudioburstPlayer.loadPlaylist(byteArray)
```
The `loadPlaylist` function accepts `Byte Array` as an argument. A request included in the PCM file will be processed and the player will load a playlist of the bursts found. If no bursts are found, `ErrorListener` will be called.
Please remember that before playing any PCM file the SDK must be initialized.

### Step 7. Programatically control Floating (Button) player
When you choose to use Floating (Button) Player you can better control its position and state with the set of the functions described below.

#### `setPlayerPosition`
This function accepts x and y coordinates. It will let you move Floating player around and place it wherever you want. If you call this function before Floating player is shown you will make library remember the position and it will show it at requested position as soon as player is initialized.
```kotlin
AudioburstPlayer.setPlayerPosition(0, 0)
```

#### `setPlayerState`
Floating player can be displayed in one of the following states:
- `Floating` - the default state. When it is being shown as a small circle.
- `Expanded` - the state where additional information and playback control buttons are displayed.
- `Sticky` - minimized player that is attached to one of the side edges.
It is possible to transit between following states:
- From `Floating` to `Expanded` - it will animate a player expand.
- From `Expanded` to `Floating` - it will animate a player collapse.
- From `Floating` to `Sticky` - it will find the closest edge and attach to it.
- From `Sticky` to `Floating` - it will detach from to edge.

You can control the appearance by using this function:
```kotlin
AudioburstPlayer.setPlayerState(Floating)
```

If you call this function before Floating player is shown you will make library remember the requested state and it will show in it as soon as player is initialized.

#### `getPlayerStatus`
This function will let you know what is the `PlayerStatus`:
positionX - Int - current X coordinate of the Floating player.
positionY - Int - current Y coordinate of the Floating player.
playerState - PlayerState - current state of the Floating player.
lastActivationDate - LocalDateTime? - last time when Floating player has been used by the user. It can be null if there was no action performed on the Floting player yet.
This function can return null when Floating player is not shown yet.

### Step 8. Handle errors
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

## Additional configuration

## Filter out listened Bursts
By default, SDK will filter-out all Bursts that user already listened to. Use `filterListenedBursts` function to change this behaviour.
```kotlin
AudioburstPlayer.filterListenedBursts(isEnabled)
```

### Disable/enable offline playback
Before you initialize AudioburstPlayer you can decide whether SDK should prefetch Bursts to allow playback even when the user loses the Internet connection. By default the SDK will allow for this, but you can change this behavior using the following method:
```kotlin
AudioburstPlayer.allowOfflinePlayback = false
```

### Disable/enable playback notification
You can decide whether library should display playback notification. `true` means that playback will happen in Foreground Service.
```kotlin
AudioburstPlayer.allowDisplayPlaybackNotification = false
```
This value is set to `true` by default.

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