# light-sdk
or: a tool for building Tools

## tl;dr
This repository contains the scaffolding for building simple tools for the Light Phone III. Included are a library ([:sdk:client](./sdk/client)) and placeholder application ([:tool](./tool)) that depends on it. To create a tool that is fully compatible with LightOS, you must write your application code within the `tool` module, using the primitives provided by the sdk client library.

You can and should use current Android best practices: Kotlin for all source code, Compose for UI, Coroutines for async programming, and MVVM architecture. **Although this is appears to be a fairly standard Android dev environment, you will quickly find out that we are (gently but broadly) restricting which Android APIs and third-party libraries can be used. This is in an effort to provide a secure and distinctly _light_ experience for our users. These restrictions are _not_ set in stone and should ease up over time. If there is a stable, open-source library that you'd like us to allow, please let us know! More on this later.**

## Quickstart
### Running your Tool
**You can test your tool on any Android device or emulator**, but certain functionality (receiving push notifications, requesting special permissions) can only be tested with:
A) Real Light Phone hardware running LightOS
B) An Android emulator (on your computer) set up to run our LightOS emulator app as a _system app_ (see advanced instructions below)

If you want to wait on those options, you can [create an emulator](https://developer.android.com/studio/run/managing-avds) that generally feels like an LP3:
* 1080 X 1240, 3.92" display
* Android API 34
* NO Google Play Services installed

### Start Building
1. Fork and/or clone this repository into your local dev environment.
2. Install Android Studio and open this project within it. (IntelliJ IDEA should also work)

3. Edit the code in `HomeScreen` and `HomeScreenViewModel` to get started. `Homescreen` surfaces a `@Composable` method named `Content`. This is the UI that is shown when the tool first boots. You'll notice this UI sources data from it's `viewModel` field, which is an instance of `HomeScreenViewModel`. Edit that class with your screen's logic and expose the data to the UI using either Compose `State` or Coroutine `Flow`s. If you want to create a new screen, create a new Screen/ViewModel pair: your screen should extend from `LightScreen` and your VM from `LightScreenViewModel`. Your screen implementation will need:
   1. A direct reference to your ViewModel's class type
   2. A factory method for creating a new instance of your ViewModel.

Look at `HomeScreen` as an example for how this is done. To navigate to your new screen, use the `navigateTo` function built into `LightScreen` - just pass it a lambda to create an instance of your new screen. Note that the `LightScreen` constructor takes in a `SealedLightActivity`. The lambda is provided an instance of this as a default parameter.

Since LightOS does not use Android system navigation, we provide a back button for you. As long as you use `navigateTo` to move between screens, our back button should work great. If need be, you can override `shouldShowBackButton` in your `LightScreen` and/or the `onBackPressed` method in your `LightViewModel`.

## Advanced
### Using the LightOS Emulator as a System App
On real LP3 hardware, LightOS runs as a system app, which means it has access to Android functionality that a normal app does not. It is possible (and desirable) to run our LightOS emulator software _as_ a system app on an Android device emulator on your computer. **You will not be able to do this on any consumer Android hardware**. Besides running your tool on a real LP3 running a production build of LightOS, this will give you the best idea of how your tool will work on real Light hardware. Here are the instructions for setting it up:

#### 1. Create an AVD
Create an Android Virtual Device in Android Studio with the following properties:
* **System image**: API 34 (Android 14), **without Google Play Services** (use the "AOSP" / `google_apis` or `default` target — NOT `google_apis_playstore`)
* **Architecture**: arm64-v8a or x86_64
* **Screen**: 1080 x 1240, 3.92" display (to match the Light Phone III)

> You **must** use an image built with `test-keys` (shown in `adb shell getprop ro.build.description`). Production/user-signed images will not accept the AOSP platform test key used by the emulator app.

#### 2. Boot and prepare the emulator
(Note that the `emulator` and `adb` executables should be available in your Android sdk installation)
Start the emulator with writable system partition support:

```bash
emulator -avd <your_avd_name> -writable-system
```

> The `-writable-system` flag is required to push files into `/system`. You will need to use this flag **every time** you boot the emulator.

Then set up the system partition for writing:

```bash
adb root
adb remount
```

If `adb remount` fails with a "verity" error, disable verified boot first:

```bash
adb disable-verity
adb reboot
adb root
adb remount
```

#### 3. Generate the platform signing key

The emulator app must be signed with the AOSP platform test key so that it can share `uid 1000` with the Android system. The build expects a Java keystore at `sdk/emulator/keys/platform.jks`.

Download the AOSP platform test key files:

```bash
mkdir -p sdk/emulator/keys
curl -o /tmp/platform.x509.pem https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.x509.pem
curl -o /tmp/platform.pk8 https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.pk8
```

Convert the pk8 private key to PEM format, then import both into a Java keystore:

```bash
# Convert pk8 (PKCS#8 DER) to PEM
openssl pkcs8 -inform DER -nocrypt -in /tmp/platform.pk8 -out /tmp/platform.pem

# Bundle the cert + key into a PKCS#12 file
openssl pkcs12 -export \
    -in /tmp/platform.x509.pem \
    -inkey /tmp/platform.pem \
    -name platform \
    -out /tmp/platform.p12 \
    -passout pass:android

# Import into a Java keystore
keytool -importkeystore \
    -srckeystore /tmp/platform.p12 \
    -srcstoretype PKCS12 \
    -srcstorepass android \
    -destkeystore sdk/emulator/keys/platform.jks \
    -deststoretype JKS \
    -deststorepass android

# Clean up
rm /tmp/platform.pk8 /tmp/platform.x509.pem /tmp/platform.pem /tmp/platform.p12
```

> These are the well-known AOSP **test** keys — they are not secret. They only work on emulator images built with `test-keys`.

#### 4. Build the emulator app

```bash
./gradlew :sdk:emulator:assembleDebug
```

#### 5. Install as a privileged system app

```bash
# Create the priv-app directory
adb shell mkdir -p /system/priv-app/LightOSEmulator

# Push the APK
adb push sdk/emulator/build/outputs/apk/debug/emulator-debug.apk \
    /system/priv-app/LightOSEmulator/LightOSEmulator.apk

# Reboot so PackageManager picks it up as a system app
adb reboot
```

After reboot, verify the app is running as system:

```bash
# Should show /system/priv-app/LightOSEmulator
adb shell pm path com.thelightphone.sdk.emulator

# Should show uid=1000
adb shell dumpsys package com.thelightphone.sdk.emulator | grep "uid="
```

#### 6. Reinstalling after changes
When you rebuild the emulator app, you can update it without a full reboot:

```bash
./gradlew :sdk:emulator:assembleDebug
adb install -r sdk/emulator/build/outputs/apk/debug/emulator-debug.apk
```

The app will retain its system uid as long as the signing key and `sharedUserId` remain unchanged. The emulator app will log a [warning message](sdk/emulator/src/main/kotlin/com/thelightphone/sdk/emulator/MainActivity.kt) on startup if it is not running as a system app!

#### Troubleshooting

| Symptom | Fix |
|---|---|
| `uid=` shows a number other than `1000` | Fully uninstall (`adb uninstall com.thelightphone.sdk.emulator`), remove leftover data (`adb shell rm -rf /data/app/*com.thelightphone.sdk.emulator*`), re-push to priv-app, and reboot. |
| `adb remount` says "Device must be bootloader unlocked" | Run `adb disable-verity && adb reboot`, then `adb root && adb remount`. |
| App doesn't appear after reboot | Check that the APK was pushed to the correct path: `/system/priv-app/LightOSEmulator/LightOSEmulator.apk` (directory name and file name both matter). |
| Signatures don't match (`dumpsys` shows different sig hashes) | Make sure you are using an AOSP `test-keys` system image, not a production-signed image. Run `adb shell getprop ro.build.description` — it should end with `test-keys`. |
