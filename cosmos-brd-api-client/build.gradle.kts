plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

android {
    compileSdk = brd.BrdRelease.ANDROID_COMPILE_SDK
    buildToolsVersion = brd.BrdRelease.ANDROID_BUILD_TOOLS
    defaultConfig {
        minSdk = brd.BrdRelease.ANDROID_MINIMUM_SDK
        buildConfigField("int", "VERSION_CODE", "${brd.BrdRelease.versionCode}")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    android()
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
                implementation(kotlin("stdlib-common"))
                implementation(brd.Libs.Kotlinx.SerializationRuntime)
                implementation(brd.Libs.Coroutines.Core) {
                    version { strictly(brd.COROUTINES_VERSION) }
                }
                api(brd.Libs.Kotlinx.DateTime)
                api(brd.Libs.Ktor.Client.Core)
                api(brd.Libs.Ktor.Client.Json)
                api(brd.Libs.Ktor.Client.Serialization)
                api(brd.Libs.Ktor.Client.Logging)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("androidMain") {
            dependencies {
                implementation(brd.Libs.Ktor.Client.OkHttpClientEngine)
            }
        }

        named("androidTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        named("iosMain") {
            dependencies {
                implementation(brd.Libs.Ktor.Client.IosClientEngine)
            }
        }
    }
}
