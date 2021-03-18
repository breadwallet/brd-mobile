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

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION")?.split("-")?.first() ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val framework = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}

tasks.getByName("build").dependsOn(packForXcode)
