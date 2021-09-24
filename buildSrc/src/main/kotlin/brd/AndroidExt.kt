package brd

import org.gradle.api.Project
import java.io.*
import java.util.Properties

//Mac: ~/Library/Android/sdk
//Windows: %LOCALAPPDATA%\Android\sdk
val Project.androidSdkPath: String? get() {
    val sdkPath = readSDKFromLocalProperties(project.rootDir)
    if (sdkPath != null) return sdkPath
    ensureAndroidLocalPropertiesWithSdkDir()
    return readSDKFromLocalProperties(project.rootDir)
}

fun Project.hasAndroidSdK() = androidSdkPath != null

fun Project.ensureAndroidLocalPropertiesWithSdkDir(outputFolder: File = project.rootDir): Boolean {
    val path = project.tryToDetectAndroidSdkPath()
    return if (path != null) {
        makeLocalProperties(outputFolder, path)
        makeLocalProperties(File(outputFolder, "/external/walletkit/WalletKitJava"), path)
        true
    } else false
}

fun Project.tryToDetectAndroidSdkPath(): File? {
    for (tryAndroidSdkDirs in tryAndroidSdkDirs) {
        if (tryAndroidSdkDirs.exists()) {
            return tryAndroidSdkDirs.absoluteFile
        }
    }
    return null
}

private var _tryAndroidSdkDirs: List<File>? = null
val tryAndroidSdkDirs: List<File> get() {
    if (_tryAndroidSdkDirs == null) {
        val userHome = System.getProperty("user.home")
        _tryAndroidSdkDirs = listOfNotNull(
            System.getenv("ANDROID_HOME")?.run(::File),
            System.getenv("ANDROID_SDK_ROOT")?.run(::File),
            userHome?.let { File(it , "/Library/Android/sdk") }, // MacOS
            userHome?.let { File(it, "/AppData/Local/Android/Sdk") } // Windows
        )
    }
    return _tryAndroidSdkDirs ?: emptyList()
}

fun readSDKFromLocalProperties(
    outputFolder: File
) : String? {
    val localPropertiesFile = File(outputFolder, "local.properties")
    if (localPropertiesFile.exists()) {
        val props = Properties().apply { load(localPropertiesFile.readText().reader()) }
        props.getProperty("sdk.dir")?.let { return it }
    }
    return null
}

fun makeLocalProperties(
    outputFolder: File,
    sdkPath: File
) {
    val localProperties = File(outputFolder, "local.properties")
    if (!localProperties.exists()) {
        localProperties
            .ensureParents()
            .writeText("sdk.dir=${sdkPath.absolutePath.replace("\\", "/")}")
    }
}

fun File.ensureParents() = this.apply { parentFile.mkdirs() }
