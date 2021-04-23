plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
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
                implementation(kotlin("stdlib-common"))
                implementation(brd.Libs.Kotlinx.SerializationRuntime)
                implementation(brd.Libs.Coroutines.Core)
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

        named("jvmMain") {
            dependencies {
                implementation(brd.Libs.Ktor.Client.OkHttpClientEngine)
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
                implementation(brd.Libs.Ktor.Client.IosClientEngine)
            }
        }

        named("iosTest") {
            dependencies {
                implementation("com.autodesk:coroutineworker:0.6.2")
            }
        }
    }
}
