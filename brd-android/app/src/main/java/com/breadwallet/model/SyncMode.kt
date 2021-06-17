/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 12/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

import com.breadwallet.crypto.WalletManagerMode

enum class SyncMode(val walletManagerMode: WalletManagerMode) {
    API_ONLY(WalletManagerMode.API_ONLY),
    P2P_ONLY(WalletManagerMode.P2P_ONLY);

    companion object {
        private val map = values().associateBy(SyncMode::walletManagerMode)
        fun fromWalletManagerMode(mode: WalletManagerMode) = checkNotNull(map[mode])
    }
}
