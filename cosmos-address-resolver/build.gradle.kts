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
                implementation(project(":cosmos-core"))
                implementation(brd.Libs.Kotlin.StdLibJdk8)
                implementation(brd.Libs.Coroutines.Core)
                implementation(brd.Libs.Ktor.Client.Core)
                implementation(brd.Libs.Ktor.Client.Json)
                implementation(brd.Libs.Ktor.Client.Serialization)
                implementation(brd.Libs.Blockset)
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(brd.Libs.Ktor.Client.OkHttpClientEngine)
            }
        }

        named("iosMain") {
            dependencies {
                implementation(brd.Libs.Ktor.Client.IosClientEngine)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}
