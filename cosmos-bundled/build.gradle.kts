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
