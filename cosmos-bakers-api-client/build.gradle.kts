import brd.Libs

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
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
                implementation(Libs.Coroutines.Core) {
                    version { strictly(brd.COROUTINES_VERSION) }
                }
                implementation(Libs.Kotlinx.SerializationRuntime)
                api(Libs.Ktor.Client.Core)
                implementation(Libs.Ktor.Client.Json)
                implementation(Libs.Ktor.Client.Serialization)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(Libs.Kotlin.TestCommon)
                implementation(Libs.Kotlin.TestAnnotationsCommon)
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(Libs.Ktor.Client.OkHttpClientEngine)
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        named("iosMain") {
            dependencies {
                implementation(Libs.Ktor.Client.IosClientEngine)
            }
        }
    }
}
