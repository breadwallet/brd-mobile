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
                api(project(":cosmos-api-client"))
                api(project(":cosmos-bakers-api-client"))
            }
        }
    }
}
