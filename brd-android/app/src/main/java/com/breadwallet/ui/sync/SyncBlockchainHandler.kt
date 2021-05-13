/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.sync

import com.breadwallet.breadbox.BreadBox
import com.blockset.walletkit.WalletManagerSyncDepth.FROM_CREATION
import com.breadwallet.ui.sync.SyncBlockchain.E
import com.breadwallet.ui.sync.SyncBlockchain.F
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.flow.first

fun createSyncBlockchainHandler(breadBox: BreadBox) = subtypeEffectHandler<F, E> {
    addFunction<F.SyncBlockchain> { effect ->
        val wallet = breadBox.wallet(effect.currencyCode).first()
        wallet.walletManager.syncToDepth(FROM_CREATION)
        E.OnSyncStarted
    }
}
