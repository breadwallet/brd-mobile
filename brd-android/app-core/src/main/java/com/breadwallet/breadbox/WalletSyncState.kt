/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

data class WalletSyncState(
    val currencyCode: String,
    val isSyncing: Boolean,
    val percentComplete: Float,
    val timestamp: Long
)