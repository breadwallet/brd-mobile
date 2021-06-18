/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

private const val DOWNLOAD_URL = "https://%s/assets/bundles/%s/download"
private const val RAW_PATH = "src/%s/res/raw"
private val bundles = listOf("brd-web-3", "brd-tokens")

open class DownloadBundles : DefaultTask() {

    @TaskAction
    fun run() {
        download("api.breadwallet.com", "main")
        download("stage2.breadwallet.com", "debug", "-staging")
        downloadTokens()
    }

    private fun downloadTokens() {
        val rawFolder = File(project.projectDir, RAW_PATH.format("main")).apply {
            if (!exists()) check(mkdirs()) {
                "Failed to create resource directory: $absolutePath"
            }
        }
        val currenciesMainnetFile = File(rawFolder, "tokens.json")
        val currenciesTestnetFile = File(rawFolder, "tokens_testnet.json")
        URL("https://api.breadwallet.com/currencies").saveTo(currenciesMainnetFile)
        currenciesTestnetFile.bufferedWriter().use { writer ->
            currenciesMainnetFile.bufferedReader().use { reader ->
                writer.append(
                    reader.readLine()
                        .replace("ethereum-mainnet:", "ethereum-ropsten:")
                        .replace("-mainnet:__native__", "-testnet:__native__")
                )
            }
        }
    }

    private fun download(host: String, sourceFolder: String, bundleSuffix: String = "") {
        val resFolder = File(project.projectDir, RAW_PATH.format(sourceFolder)).apply {
            if (!exists()) check(mkdirs()) {
                "Failed to create resource directory: $absolutePath"
            }
        }

        bundles.map { bundle ->
            val bundleName = "$bundle$bundleSuffix"
            val fileName = bundleName.replace("-", "_")
            val downloadUrl = DOWNLOAD_URL.format(host, bundleName)
            URL(downloadUrl).saveTo(File(resFolder, "$fileName.tar"))
        }
    }
}

/** Copy contents at a [URL] into a local [File]. */
fun URL.saveTo(file: File): Unit = openStream().use { input ->
    file.outputStream().use { input.copyTo(it) }
}
