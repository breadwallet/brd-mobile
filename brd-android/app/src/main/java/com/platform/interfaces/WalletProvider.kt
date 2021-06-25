/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.interfaces

import com.breadwallet.crypto.WalletManagerMode
import kotlinx.coroutines.flow.Flow

interface WalletProvider {

    fun enabledWallets(): Flow<List<String>>
    fun walletModes(): Flow<Map<String, WalletManagerMode>>
}
