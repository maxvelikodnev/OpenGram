## Telegram messenger for Android

### OpenGram: personal Android build

This personal fork is displayed as **Telegram+**, package `app.opengram.android`,
with the original Telegram launcher icon. It remains an independent, unofficial
client; the repository and local build tooling are named OpenGram.
The prepared variant is `TMessagesProj_App:afatRelease`, a universal APK for
**arm64-v8a, armeabi-v7a, x86 and x86_64**, running Android 6.0 or newer.
It installs alongside official Telegram.

Account storage is dynamic in both Java and the native networking layer.
Adding an account does not require Premium and has no configured account-count
cap. Existing slot IDs, preferences and database paths are preserved; empty
slots are reused and newly allocated slots are persisted across restarts.
Actual capacity depends on device memory, storage and background-process limits.
Other Telegram Premium features and server-enforced limits are unchanged.

Account-storage verification:

```bash
bash scripts/test-accounts.sh
# After installing the release on an attached Android device:
python3 scripts/test-accounts-device.py DEVICE_SERIAL
```

The device check uses a temporary signed instrumentation package to initialize
independent JNI slots 64 and 65, without logging in additional users. It removes
only its own temporary test package afterwards.

From the repository directory:

```bash
# Validate settings, download dependencies, prepare resources and CMake.
# This does not compile or package an APK.
./scripts/opengram.sh prepare

# When ready, compile and sign the APK.
./scripts/opengram.sh apk
```

Expected APK: `TMessagesProj_App/build/outputs/apk/afat/release/app.apk`.

The script uses Java 17 (Homebrew on this Mac), Gradle Wrapper 8.13 and an
ignored `.gradle-home` directory. Set `OPENGRAM_JAVA_HOME` for a different Java
17 installation. Android SDK 36, Build Tools 36.0.0, NDK 27.2.12479018 and
CMake 3.22.1 and NASM must be installed. Configure `sdk.dir` in `local.properties`.

Local-only files, already prepared on this Mac:

- `local.properties`: Telegram API ID/hash, SDK path and signing passwords.
- `TMessagesProj/src/release/google-services.json` and
  `TMessagesProj_App/src/release/google-services.json`: OpenGram Firebase config.
- `../opengram-signing.p12`: the dedicated APK signing key. Preserve this key
  and its passwords so future APKs can update the installed app.
- `../opengram-fcm-service-account.json`: server-side FCM private key, already
  registered with Telegram. Never include it in the APK or the repository.

Use `local.properties.example` when setting up a new machine. Local secrets,
Firebase configuration and signing keys are excluded from Git. The actual
Telegram API credentials are injected into generated BuildConfig during build;
they are not stored in the committed source, but are necessarily part of the APK.

Official-client-only passkeys and Firebase phone-number verification, Google
Play Billing and official Telegram auto-updates are disabled. FCM messaging
remains enabled. Embedded Google Maps needs your own optional
`OPENGRAM_MAPS_API_KEY` in `local.properties`; no Telegram Maps key is used by
the prepared variant. Notification delivery still needs a test on the phone
after compiling and installing the APK.

[Telegram](https://telegram.org) is a messaging app with a focus on speed and security. It’s superfast, simple and free.
This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Creating your Telegram Application

We welcome all developers to use our API and source code to create applications on our platform.
There are several things we require from **all developers** for the moment.

1. [**Obtain your own api_id**](https://core.telegram.org/api/obtaining_api_id) for your application.
2. Please **do not** use the name Telegram for your app — or make sure your users understand that it is unofficial.
3. Kindly **do not** use our standard logo (white paper plane in a blue circle) as your app's logo.
3. Please study our [**security guidelines**](https://core.telegram.org/mtproto/security_guidelines) and take good care of your users' data and privacy.
4. Please remember to publish **your** code too in order to comply with the licences.

### API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto

### Compilation Guide

**Note**: In order to support [reproducible builds](https://core.telegram.org/reproducible-builds), this repo contains dummy release.keystore,  google-services.json and filled variables inside BuildVars.java. Before publishing your own APKs please make sure to replace all these files with your own.

You will require Android Studio 2025.1.4, Android NDK 27.2.12479018 and Android SDK 36.

1. Clone the Telegram source code with its submodules:
   ```bash
   git clone --recursive --shallow-submodules https://github.com/DrKLO/Telegram.git Telegram
   ```
   In case you forgot the `--recursive` flag, change to the `Telegram` directory and run:
   ```bash
   git submodule init && git submodule update --init --recursive --depth=1
   ```
2. Copy your release.keystore into TMessagesProj/config
3. Fill out RELEASE_KEY_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_STORE_PASSWORD in gradle.properties to access your  release.keystore
4.  Go to https://console.firebase.google.com/, create two android apps with application IDs org.telegram.messenger and org.telegram.messenger.beta, turn on firebase messaging and download google-services.json, which should be copied to the same folder as TMessagesProj.
5. Open the project in the Studio (note that it should be opened, NOT imported).
6. Fill out values in TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java – there’s a link for each of the variables showing where and which data to obtain.
7. You are ready to compile Telegram.

### Localization

We moved all translations to https://translations.telegram.org/en/android/. Please use it.
