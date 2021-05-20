plugins {
    kotlin("multiplatform") version brd.KOTLIN_VERSION apply false
    kotlin("plugin.serialization") version brd.KOTLIN_VERSION apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal() // TODO: required by com.android.tools.lint for org.jetbrains.trove4j:trove4j:20160824
    }
}

