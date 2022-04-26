/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 10/24/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.effecthandler.metadata

import com.blockset.walletkit.Transfer
import com.blockset.walletkit.WalletManagerMode
import java.math.BigDecimal

sealed class MetaDataEffect {

    data class LoadTransactionMetaData(
        val currencyCode: String,
        val transactionHashes: List<String>
    ) : MetaDataEffect() {
        constructor(currencyCode: String, transactionHash: String) : this(
            currencyCode,
            listOf(transactionHash)
        )
    }

    data class LoadTransactionMetaDataSingle(
        val currencyCode: String,
        val transactionHashes: List<String>
    ) : MetaDataEffect()

    data class AddTransactionMetaData(
        val transaction: Transfer,
        val comment: String,
        val fiatCurrencyCode: String,
        val fiatPricePerUnit: BigDecimal
    ) : MetaDataEffect()

    data class UpdateTransactionComment(
        val currencyCode: String,
        val transactionHash: String,
        val comment: String
    ) : MetaDataEffect()

    data class UpdateWalletMode(
        val currencyId: String,
        val mode: WalletManagerMode
    ) : MetaDataEffect()

    object LoadWalletModes : MetaDataEffect()
}
