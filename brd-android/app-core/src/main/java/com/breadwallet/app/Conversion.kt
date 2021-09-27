/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 7/22/2020.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.app

import com.breadwallet.breadbox.hashString
import com.breadwallet.breadbox.toBigDecimal
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.TransferState
import java.util.Date

private const val PREFIX_DELIMITER = "-"
private const val BUY_CLASS_PREFIX = "buy"
private const val TRADE_CLASS_PREFIX = "trade"

sealed class Conversion {
    abstract val currencyCode: String
    abstract fun isTriggered(transfer: Transfer): Boolean
    abstract fun serialize(): String

    companion object {
        fun deserialize(value: String): Conversion {
            return when (value.substringBefore(PREFIX_DELIMITER)) {
                BUY_CLASS_PREFIX -> Buy.deserialize(value)
                TRADE_CLASS_PREFIX -> Trade.deserialize(value)
                else -> throw IllegalStateException("Unknown Type")
            }
        }
    }
}

data class Buy(override val currencyCode: String, val amount: Double, val timestamp: Long) :
    Conversion() {
    override fun isTriggered(transfer: Transfer) =
        transfer.amount.toBigDecimal().toDouble() == amount &&
            transfer.state.type == TransferState.Type.INCLUDED &&
            transfer.confirmation.get().confirmationTime.after(Date(timestamp)) &&
            transfer.wallet.currency.code.equals(currencyCode, true)

    override fun serialize() = "$BUY_CLASS_PREFIX$PREFIX_DELIMITER$currencyCode;$amount;$timestamp"

    companion object {
        fun deserialize(value: String): Buy {
            val (currencyCode, amount, timestamp) =
                value.substringAfter(PREFIX_DELIMITER).split(";")
            return Buy(currencyCode, amount.toDouble(), timestamp.toLong())
        }
    }
}

data class Trade(override val currencyCode: String, val hashString: String) : Conversion() {
    override fun isTriggered(transfer: Transfer) =
        transfer.hashString().equals(hashString, true) &&
            transfer.state.type == TransferState.Type.INCLUDED

    override fun serialize() = "$TRADE_CLASS_PREFIX$PREFIX_DELIMITER$currencyCode;$hashString"

    companion object {
        fun deserialize(value: String): Trade {
            val (currencyCode, hashString) = value.substringAfter(PREFIX_DELIMITER).split(";")
            return Trade(currencyCode, hashString)
        }
    }
}
