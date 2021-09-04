repositories {
    mavenCentral()
    google()
    jcenter()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.5.20"
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.android.tools.build:gradle:7.0.2")
    implementation("org.json:json:20201115")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("io.ktor:ktor-client-okhttp:1.6.3")
    implementation("io.ktor:ktor-client-serialization:1.6.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

