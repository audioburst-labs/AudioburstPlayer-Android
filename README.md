# AudioburstPlayer Android SDK

## Introduction
AudioburstPlayer is a SDK for Android that will let you play your previously prepared playlist of Bursts. This repo is the sample which demonstrate how to use AudioburstPlayer in your app.

## Features
AudioburstPlayer consists of two modes of audio player - compact and fullscreen which allows you to:
- download a custom playlist,
- play any burst from the playlist,
- skip to the next or previous burst,
- switch to original listening,
- move backward and forward within a single burst,
- preview the title, the show name,
- play the playlist continuously in a background,
- control the playlist from the locked screen,
- plug the headphones or cast the audio to other devices via bluetooth.

## Get Started

This guide is a quick start to add AudioburstPlayer to an Android app. Android Studio is the recommended development environment for building an app with the AudioburstPlayer SDK.

## Prerequisites

### Audioburst API key
Your application needs an **application key** (check [Audioburst developers site](https://developers.audioburst.com/) to obtain the key).
Also you need to provide **experience id**.

## Add AudioburstPlayer to your app

### Step 1. Add AudioburstPlayer dependency
Add AudioburstPlayer Android SDK to your project. To do this, add the following dependency in your app level `build.gradle` file-
```gradle
implementation 'com.audioburst:audioburst_player:{latest-version}'
```

### Step 2. Init AudioburstPlayer
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

Also you need to implement `AudioburstApplication` in your `Application` class and pass dependencies like that:
```kotlin
override val dependencies: Dependencies
    get() = AudioburstPlayer.dependencies()
```

### Step 3. Start playing Audioburst content:
You simply need to call one method to start playing Audioburst content:
```kotlin
AudioburstPlayer.startPlaying()
```

in case you would like to change keys you used to initialize a SDK, you can also call:
```kotlin
AudioburstPlayer.startPlaying(
    sdkKeys = SdkKeys(
        applicationKey = "YOUR_APP_KEY",
        experienceId = "YOUR_EXPERIENCE_ID"
    )
)
```

### Step 4. Handle errors
There can some errors happen when, for example, communicating with the API, so to be able to handle those errors on app side we are providing a way to listen to those events:
Make your class implement `AudioburstPlayer.ErrorListener`, for example:
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
Also remember to unregister ErrorListener to aviod memory leaks:
```kotlin
AudioburstPlayer.removeErrorListener(this)
```


License
-------

    Copyright 2020 Audioburst

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.