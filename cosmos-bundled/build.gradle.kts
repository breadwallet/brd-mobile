import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    ios {
        compilations.named("main") {
            binaries.framework {
                baseName = "Cosmos"
                export(project(":cosmos-api-client"))
                export(project(":cosmos-bakers-api-client"))
            }
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                // Work around a transitive dependency on Atomicfu that
                // causes the Kotlin compiler to fail when proccing an
                // outdated klib package.  This is reported twice as
                // https://youtrack.jetbrains.com/issue/KT-43911 and
                // https://youtrack.jetbrains.com/issue/KT-41821
                implementation(brd.Libs.Kotlinx.Atomicfu)

                api(project(":cosmos-api-client"))
                api(project(":cosmos-bakers-api-client"))
            }
        }
    }
}

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
    val xcFrameworkDestination = File(buildDir, "xcode-frameworks/${outputName}.xcframework")
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