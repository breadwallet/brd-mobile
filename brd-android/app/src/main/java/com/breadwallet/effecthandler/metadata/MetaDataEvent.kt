/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 11/18/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.effecthandler.metadata

import com.breadwallet.crypto.WalletManagerMode
import com.breadwallet.platform.entities.TxMetaData

sealed class MetaDataEvent {
    data class OnTransactionMetaDataUpdated(
        val transactionHash: String,
        val txMetaData: TxMetaData
    ) : MetaDataEvent()

    data class OnTransactionMetaDataSingleUpdated(
        val metadata: Map<String, TxMetaData>
    ) : MetaDataEvent()

    data class OnWalletModesUpdated(
        val modeMap: Map<String, WalletManagerMode>
    ) : MetaDataEvent()
}
