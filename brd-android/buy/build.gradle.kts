import brd.BrdRelease
import brd.Libs

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("dev.zacsweers.redacted")
}

redacted {
    replacementString.set("***")
}

android {
    compileSdkVersion(BrdRelease.ANDROID_COMPILE_SDK)
    buildToolsVersion(BrdRelease.ANDROID_BUILD_TOOLS)
    defaultConfig {
        minSdkVersion(BrdRelease.ANDROID_MINIMUM_SDK)
        buildConfigField("int", "VERSION_CODE", "${BrdRelease.versionCode}")
    }
    lintOptions {
        isAbortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Libs.Androidx.AppCompat)
    implementation(Libs.Androidx.CoreKtx)
    implementation(Libs.Androidx.LifecycleLiveDataKtx)
    implementation(Libs.Androidx.LifecycleViewModelKtx)

    implementation(Libs.Material.Core)

    implementation(Libs.Networking.Moshi)
    implementation(Libs.Networking.Retrofit)
}