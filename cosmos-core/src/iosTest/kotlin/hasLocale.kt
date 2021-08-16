/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import platform.Foundation.NSLocale
import platform.Foundation.availableLocaleIdentifiers


actual fun hasLocale(id: String): Boolean {
    return NSLocale.availableLocaleIdentifiers.any { it == id }
}
