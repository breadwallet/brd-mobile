/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.prefs

internal actual val platformKeyMap = mapOf(
    BrdPreferences.KEY_DEVICE_ID to "userId",
    BrdPreferences.KEY_USER_FIAT to "currentCurrency",
)