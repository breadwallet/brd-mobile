/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import android.content.Context
import android.content.res.AssetManager
import com.brd.support.SupportEffectHandler

class AndroidSupportDataProvider(private val context: Context) : SupportDataProvider {

    override fun load(fileName: String): String {
        context.assets.open(
            "support/$fileName",
            AssetManager.ACCESS_STREAMING
        ).bufferedReader().use {
            return it.readText()
        }
    }
}