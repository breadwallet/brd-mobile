/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 8/17/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.breadwallet.util.isBitcoin
import com.breadwallet.util.isErc20
import com.breadwallet.util.isEthereum
import java.util.concurrent.TimeUnit

private const val PREFIX_DELIMITER = "-"

sealed class TransferSpeed {
    abstract val targetTime: Long
    abstract val currencyCode: String
    override fun toString(): String {
        return "${this::class.simpleName}$PREFIX_DELIMITER$currencyCode"
    }

    companion object {
        fun valueOf(value: String): TransferSpeed {
            val currencyCode = value.substringAfter(PREFIX_DELIMITER)
            return when (value.substringBefore(PREFIX_DELIMITER)) {
                Economy::class.simpleName -> Economy(currencyCode)
                Regular::class.simpleName -> Regular(currencyCode)
                Priority::class.simpleName -> Priority(currencyCode)
                else -> error("Unknown Type")
            }
        }
    }

    class Economy(override val currencyCode: String) :  TransferSpeed() {
        override val targetTime = when {
            currencyCode.run { isEthereum() || isErc20() } -> TimeUnit.MINUTES.toMillis(5L)
            else -> TimeUnit.HOURS.toMillis(7L)
        }
    }

    class Regular(override val currencyCode: String): TransferSpeed() {
        override val targetTime = when {
            currencyCode.isBitcoin() -> TimeUnit.MINUTES.toMillis(30L)
            else -> TimeUnit.MINUTES.toMillis(3L)
        }
    }

    class Priority(override val currencyCode: String) :  TransferSpeed() {
        override val targetTime = when {
            currencyCode.run { isEthereum() || isErc20() } -> TimeUnit.MINUTES.toMillis(1L)
            else -> 0L
        }
    }
}