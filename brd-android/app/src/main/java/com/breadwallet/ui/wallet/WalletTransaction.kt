/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/21/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import com.breadwallet.platform.entities.GiftMetaData
import dev.zacsweers.redacted.annotations.Redacted
import java.math.BigDecimal

data class WalletTransaction(
    @Redacted val txHash: String,
    val amount: BigDecimal,
    val amountInFiat: BigDecimal,
    @Redacted val toAddress: String,
    @Redacted val fromAddress: String,
    val isReceived: Boolean,
    @Redacted val timeStamp: Long,
    @Redacted val memo: String? = null,
    val fee: BigDecimal,
    val confirmations: Int,
    val isComplete: Boolean,
    val isPending: Boolean,
    val isErrored: Boolean,
    val progress: Int,
    val currencyCode: String,
    val feeToken: String = "",
    val confirmationsUntilFinal: Int,
    val gift: GiftMetaData? = null,
    val isStaking: Boolean = false
) {
    val isFeeForToken: Boolean = feeToken.isNotBlank()
}
