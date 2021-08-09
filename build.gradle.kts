plugins {
    kotlin("multiplatform") version brd.KOTLIN_VERSION apply false
    kotlin("plugin.serialization") version brd.KOTLIN_VERSION apply false
    id("org.jlleitschuh.gradle.ktlint") version brd.KTLINT_VERSION
}

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal() // TODO: required by com.android.tools.lint for org.jetbrains.trove4j:trove4j:20160824
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

afterEvaluate {
    tasks["clean"].dependsOn(tasks.getByName("ktlintApplyToIdea"))
    tasks["clean"].dependsOn(tasks.getByName("addKtlintFormatGitPreCommitHook"))
}
