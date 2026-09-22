# Mobile-only APK build

This project includes a GitHub Actions workflow that can compile the Android APK
without Android Studio on your phone.

1. Create a GitHub repository from your phone.
2. Upload this project and push to the `main` branch.
3. Open the repository's **Actions** tab.
4. Run **Build Android APK** (or wait for the push build).
5. Open the completed workflow run.
6. Download the `FreePS3Emulator-debug` artifact.
7. Extract the ZIP and install `app-debug.apk` on your Android phone.

The generated app is a foundation, not a playable PS3 emulator yet.
No Sony firmware, encryption keys, or copyrighted games are included.
Use only files you are legally entitled to use.
