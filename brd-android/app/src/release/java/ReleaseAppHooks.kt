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
import okhttp3.Interceptor

internal fun BreadApp.installHooks() {
}

fun initializeFlipper(context: Context) = Unit // No-Op for Release builds

fun getFlipperOkhttpInterceptor(): Interceptor? = null
