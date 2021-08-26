plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    compileSdkVersion(brd.BrdRelease.ANDROID_COMPILE_SDK)
    buildToolsVersion(brd.BrdRelease.ANDROID_BUILD_TOOLS)
    defaultConfig {
        minSdkVersion(brd.BrdRelease.ANDROID_MINIMUM_SDK)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        val main by getting { setRoot("src/androidMain") }
    }
}

kotlin {
    android { publishAllLibraryVariants() }
    jvm()
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }

        val jvmCommonTest by creating {
            dependsOn(jvmCommonMain)
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
        }

        val jvmTest by getting {
            dependsOn(jvmCommonTest)
        }

        val androidMain by getting {
            dependsOn(jvmCommonMain)
        }

        val androidTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(brd.Libs.AndroidxTest.Runner)
            }
        }
    }
}
