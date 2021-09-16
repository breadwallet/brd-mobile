/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 09/15/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.util

import android.content.Context

fun Context.getPixelsFromDps(dps: Int): Int {
    val scale = resources.displayMetrics.density
    return (dps * scale + 0.5f).toInt()
}
