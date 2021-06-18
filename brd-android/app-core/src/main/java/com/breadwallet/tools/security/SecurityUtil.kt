/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.security

import android.content.Context
import com.breadwallet.tools.util.Utils

fun isFingerPrintAvailableAndSetup(context: Context): Boolean {
    return Utils.isFingerprintAvailable(context) && Utils.isFingerprintEnrolled(context)
}


