plugins {
    kotlin("multiplatform") /*version "1.4.31"*/ apply false
    kotlin("plugin.serialization") version "1.4.31" apply false
    id("org.jetbrains.dokka") version "1.4.20"
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlinx/") // TODO: required for kotlinx.datetime
        maven(url = "https://dl.bintray.com/kotlin/dokka") // TODO: required for dokka
    }
}

// Patch missing multimodule index file: https://github.com/Kotlin/dokka/issues/1469
tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    if (!name.contains("html", ignoreCase = true)) return@withType

    val docs = buildDir.resolve("dokka/htmlMultiModule")
    outputDirectory.set(docs)
    doLast {
        docs.resolve("-modules.html").copyTo(docs.resolve("index.html"))
    }
}
