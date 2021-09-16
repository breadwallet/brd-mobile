/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 09/15/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual object PlatformInfo {
    private val versionPattern = "^[0-9]{1,2}.[0-9]{1,2}.[0-9].[0-9]{1,3}$".toRegex()

    private val versionCode: MutableStateFlow<Int> = MutableStateFlow(0)

    actual fun getCode(): Int {
        return if (versionCode.value > 0) {
            versionCode.value
        } else {
            val version = NSBundle.mainBundle()
                .objectForInfoDictionaryKey("CFBundleShortVersionString").toString()
            val buildCode = NSBundle.mainBundle()
                .objectForInfoDictionaryKey("CFBundleVersion").toString()

            val fullVersion = "$version.$buildCode"
            return if (fullVersion.matches(versionPattern)) {
                val versionSplit = fullVersion.split(".")
                val marketing = versionSplit[0].toInt()
                val product = versionSplit[1].toInt()
                val engineering = versionSplit[2].toInt()
                val build = versionSplit[3].toInt()

                versionCode.updateAndGet {
                    (marketing * 1000000) + (product * 10000) + (engineering * 1000) + build
                }
            } else {
                0
            }
        }
    }

    actual fun getPlatform(): String = UIDevice.currentDevice.systemName
}