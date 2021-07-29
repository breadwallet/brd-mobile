/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 11/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet

import android.content.Context
import com.breadwallet.app.BreadApp
import com.breadwallet.tools.manager.BRSharedPrefs
import com.github.anrwatchdog.ANRWatchDog
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor

internal fun BreadApp.installHooks() {
    ANRWatchDog().start()
}

fun initializeFlipper(context: Context) {
    if (BRSharedPrefs.flipperEnabledDebug) {
        // Flipper init
        SoLoader.init(context, false)
        AndroidFlipperClient.getInstance(context).apply {
            addPlugin(
                InspectorFlipperPlugin(context, DescriptorMapping.withDefaults())
            )
            addPlugin(networkFlipperPlugin)
            addPlugin(SharedPreferencesFlipperPlugin(context))
            addPlugin(NavigationFlipperPlugin.getInstance())
            addPlugin(DatabasesFlipperPlugin(context))
        }.also { it.start() }
    }
}

private val networkFlipperPlugin = NetworkFlipperPlugin()

@Suppress("RedundantNullableReturnType")
fun getFlipperOkhttpInterceptor(): Interceptor? {
    return FlipperOkhttpInterceptor(networkFlipperPlugin)
}
