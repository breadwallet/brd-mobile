import brd.hasAndroidSdK
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

// Generates "local.properties" file with path to SDK (needed for buidling outside Android Studio)
hasAndroidSdK()

android {
    compileSdk = brd.BrdRelease.ANDROID_COMPILE_SDK
    buildToolsVersion = brd.BrdRelease.ANDROID_BUILD_TOOLS
    defaultConfig {
        minSdk = brd.BrdRelease.ANDROID_MINIMUM_SDK
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
    ios {
        compilations["main"].apply {
            binaries.framework {
                baseName = "Cosmos"
                export(project(":cosmos-address-resolver"))
                export(project(":cosmos-brd-api-client"))
                export(project(":cosmos-bakers-api-client"))
                export(project(":cosmos-preferences"))
                export(project(":cosmos-core"))
                export(project(":cosmos-exchange"))
                export(project(":cosmos-support"))
                export(brd.Libs.Mobiuskt.Core)
                export(brd.Libs.Mobiuskt.Extras)
                export(brd.Libs.Blockset)
            }
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":cosmos-address-resolver"))
                api(project(":cosmos-brd-api-client"))
                api(project(":cosmos-bakers-api-client"))
                api(project(":cosmos-preferences"))
                api(project(":cosmos-core"))
                api(project(":cosmos-exchange"))
                api(project(":cosmos-support"))
                api(brd.Libs.Blockset)
                api(brd.Libs.Mobiuskt.Core)
                api(brd.Libs.Mobiuskt.Extras)

                implementation(brd.Libs.Coroutines.Core) {
                    version { strictly(brd.COROUTINES_VERSION) }
                }
            }
        }
        named("androidMain") {
            dependencies {
                api(brd.Libs.Ktor.Client.OkHttpClientEngine)
            }
        }
    }
}

tasks.register<Delete>("cleanXCFramework") {
    delete(fileTree("build-frameworks") {
        exclude("*.xcframework/Info.plist")
        exclude("**/*.framework/Info.plist")
    })
}

tasks.findByName("clean")?.dependsOn("cleanXCFramework")

tasks.register<Exec>("createXCFramework") {
    group = "build"
    description = "Creates an XCFramework for iOS x64 and arm64 targets"

    // Note: CONFIGURATION may contain `-Internal` suffix.
    val configurationName = System.getenv("CONFIGURATION")?.split("-")?.first() ?: "DEBUG"
    // Select all frameworks, assumes all are for the same platform (e.g. ios only or macos only)
    val frameworks = kotlin.targets
        .filterIsInstance<KotlinNativeTarget>()
        .filter { it.konanTarget.family.isAppleFamily }
        .flatMap {
            it.binaries.filter { binary ->
                binary.outputKind == NativeOutputKind.FRAMEWORK &&
                    binary.buildType.getName().equals(configurationName, ignoreCase = true)
            }
        }

    dependsOn(frameworks.map { it.linkTask.name })

    val outputName = frameworks.first().outputFile.nameWithoutExtension
    val xcFrameworkDestination = file("build-frameworks/${outputName}.xcframework")
    executable = "xcodebuild"
    args(
        "-create-xcframework",
        "-output",
        xcFrameworkDestination.path,
        *frameworks.flatMap { framework ->
            val dsym = File(framework.outputDirectory, "${outputName}.framework.dSYM")
            val dsymArgs = if (dsym.exists()) listOf("-debug-symbols", dsym.path) else emptyList()
            listOf(
                "-framework",
                framework.outputFile.path
            ) + dsymArgs
        }.toTypedArray()
    )

    doFirst {
        xcFrameworkDestination.deleteRecursively()
    }
}
