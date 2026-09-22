plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.freeps3emulator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.freeps3emulator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
