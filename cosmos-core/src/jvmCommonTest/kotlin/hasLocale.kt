/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import java.lang.NullPointerException
import java.util.*

actual fun hasLocale(id: String): Boolean {
    return try {
        Locale.forLanguageTag(id.replace('_', '-')) != null
    } catch (e: NullPointerException) {
        false
    }
}
