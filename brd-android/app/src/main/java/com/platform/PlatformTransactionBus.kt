/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 1/15/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform

import com.breadwallet.breadbox.TransferSpeed
import com.breadwallet.ui.send.TransferField
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.math.BigDecimal

object PlatformTransactionBus {
    private val messageChannel = BroadcastChannel<TransactionMessage>(BUFFERED)

    fun sendMessage(message: TransactionMessage) {
        messageChannel.offer(message)
    }

    fun requests() =
        messageChannel.asFlow().filterIsInstance<ConfirmTransactionMessage>()

    fun results() =
        messageChannel.asFlow().filterIsInstance<TransactionResultMessage>()
}

sealed class TransactionMessage

data class ConfirmTransactionMessage(
    val currencyCode: String,
    val fiatCode: String,
    val feeCode: String,
    val targetAddress: String,
    val transferSpeed: TransferSpeed,
    val amount: BigDecimal,
    val fiatAmount: BigDecimal,
    val fiatTotalCost: BigDecimal,
    val fiatNetworkFee: BigDecimal,
    val transferFields: List<TransferField>
) : TransactionMessage()

sealed class TransactionResultMessage : TransactionMessage() {
    data class TransactionConfirmed(
        val transaction: ConfirmTransactionMessage
    ) : TransactionResultMessage()

    data class TransactionCancelled(
        val transaction: ConfirmTransactionMessage
    ) : TransactionResultMessage()
}
