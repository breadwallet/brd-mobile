plugins {
    kotlin("multiplatform") version brd.KOTLIN_VERSION apply false
    kotlin("plugin.serialization") version brd.KOTLIN_VERSION apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
