/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 10/01/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

package com.breadwallet.util

import android.app.Activity
import android.view.WindowManager

fun Activity.setStatusBarColor(color: Int) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = getColor(color)
}
