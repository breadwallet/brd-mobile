import brd.hasAndroidSdK

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    ios()
    jvm()
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }
        named("commonMain") {
            dependencies {
                implementation(project(":cosmos-core"))
                implementation(project(":cosmos-preferences"))
                implementation(kotlin("stdlib-common"))
                implementation(brd.Libs.Coroutines.Core) {
                    version { strictly(brd.COROUTINES_VERSION) }
                }
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}
