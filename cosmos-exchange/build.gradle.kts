import brd.hasAndroidSdK

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

// Generates "local.properties" file with path to SDK (needed for buidling outside Android Studio)
hasAndroidSdK()

android {
    compileSdkVersion(brd.BrdRelease.ANDROID_COMPILE_SDK)
    buildToolsVersion(brd.BrdRelease.ANDROID_BUILD_TOOLS)
    defaultConfig {
        minSdkVersion(brd.BrdRelease.ANDROID_MINIMUM_SDK)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    ios()

    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }
        named("commonMain") {
            dependencies {
                implementation(project(":cosmos-core"))
                implementation(project(":cosmos-brd-api-client"))
                implementation(project(":cosmos-preferences"))
                implementation(project(":cosmos-websocket"))
                implementation(kotlin("stdlib-common"))
                implementation(brd.Libs.Coroutines.Core) {
                    version { strictly(brd.COROUTINES_VERSION) }
                }
                implementation(brd.Libs.Mobiuskt.Core)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("androidTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}
