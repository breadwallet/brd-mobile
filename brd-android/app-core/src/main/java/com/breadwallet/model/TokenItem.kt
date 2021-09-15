/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 12/18/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

import com.breadwallet.util.isBitcoin
import com.breadwallet.util.isBitcoinCash
import com.breadwallet.util.isDoge
import com.breadwallet.util.isEthereum
import com.breadwallet.util.isLitecoin
import com.breadwallet.util.isRipple

data class TokenItem(
    val address: String?,
    val symbol: String,
    val name: String,
    var image: String?,
    val isSupported: Boolean,
    val currencyId: String,
    val type: String = "",
    val startColor: String? = null,
    val endColor: String? = null,
    val coingeckoId: String? = null
) {

    val isNative: Boolean = type.isBlank()

    private fun urlScheme(testnet: Boolean): String? = when {
        symbol.isEthereum() || type == "erc20" -> "ethereum"
        symbol.isRipple() -> "xrp"
        symbol.isBitcoin() -> "bitcoin"
        symbol.isDoge() -> "dogecoin"
        symbol.isLitecoin() -> "litecoin"
        symbol.isBitcoinCash() -> when {
            testnet -> "bchtest"
            else -> "bitcoincash"
        }
        else -> null
    }

    fun urlSchemes(testnet: Boolean): List<String> = when {
        symbol.isRipple() -> listOfNotNull(urlScheme(testnet), "xrpl", "ripple")
        else -> urlScheme(testnet)?.run(::listOf) ?: emptyList()
    }
}
