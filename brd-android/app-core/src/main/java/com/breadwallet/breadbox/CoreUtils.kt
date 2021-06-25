/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "TooManyFunctions")

package com.breadwallet.breadbox

import com.breadwallet.crypto.Address
import com.breadwallet.crypto.Currency
import com.breadwallet.crypto.Network
import com.breadwallet.crypto.NetworkPeer
import com.breadwallet.crypto.Transfer
import com.breadwallet.crypto.TransferDirection
import com.breadwallet.crypto.Wallet
import com.breadwallet.crypto.WalletManager
import com.breadwallet.crypto.WalletManagerState
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.util.isBitcoin
import com.breadwallet.util.isBitcoinCash
import com.breadwallet.util.isEthereum
import com.breadwallet.util.isRipple
import com.google.common.primitives.UnsignedInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

/** Default port for [NetworkPeer] */
private const val DEFAULT_PORT = 8333L

/** Returns the [Address] string removing any address prefixes. */
fun Address.toSanitizedString(): String =
    toString()
        .removePrefix("bitcoincash:")
        .removePrefix("bchtest:")

/** Returns the [Transfer]'s hash or an empty string. */
fun Transfer.hashString(): String =
    checkNotNull(hash.orNull()).toString()
        .let { hash ->
            val isEthHash = wallet.currency.run { isErc20() || isEthereum() }
            when {
                isEthHash -> hash
                else -> hash.removePrefix("0x")
            }
        }

// TODO: Move somewhere UI related
fun BigDecimal.formatCryptoForUi(
    currencyCode: String,
    scale: Int = 5,
    negate: Boolean = false
): String {
    val amount = if (negate) negate() else this

    val currencyFormat = DecimalFormat.getCurrencyInstance(Locale.getDefault()) as DecimalFormat
    val decimalFormatSymbols = currencyFormat.decimalFormatSymbols
    currencyFormat.isGroupingUsed = true
    currencyFormat.roundingMode = BRConstants.ROUNDING_MODE
    decimalFormatSymbols.currencySymbol = ""
    currencyFormat.decimalFormatSymbols = decimalFormatSymbols
    currencyFormat.maximumFractionDigits = scale
    currencyFormat.minimumFractionDigits = 0
    return "${currencyFormat.format(amount)} ${currencyCode.toUpperCase()}"
}

val Wallet.currencyId: String
    get() = currency.uids

fun List<Wallet>.filterByCurrencyIds(currencyIds: List<String>) =
    filter { wallet ->
        currencyIds.any {
            it.equals(
                wallet.currencyId,
                true
            )
        }
    }

/** Returns the [Wallet] with the given [currencyId] or null. */
fun List<Wallet>.findByCurrencyId(currencyId: String) =
    find { it.currencyId.equals(currencyId, true) }

/** Returns true if any of the [Wallet]s is for the given [currencyId]. */
fun List<Wallet>.containsCurrency(currencyId: String) =
    findByCurrencyId(currencyId) != null

/** Returns true if the [WalletManager]'s [Network] supports the given [currencyId]. */
fun WalletManager.networkContainsCurrency(currencyId: String) =
    network.containsCurrency(currencyId)

/** Returns the [Currency] if the [WalletManager]'s [Network] supports the given [currencyId], null otherwise. */
fun WalletManager.findCurrency(currencyId: String) =
    network.findCurrency(currencyId)

/** Returns true if the [Network] supports the given [currencyId]. */
fun Network.containsCurrency(currencyId: String) =
    findCurrency(currencyId) != null

/** Returns the [Currency] if the [Network] supports the given [currencyId], null otherwise. */
fun Network.findCurrency(currencyId: String) =
    currencies.find { networkCurrency ->
        networkCurrency.uids.equals(
            currencyId,
            true
        )
    }

/** Returns true if the [Network] supports the given [currencyCode]. */
fun Network.containsCurrencyCode(currencyCode: String) =
    currencies.find { networkCurrency ->
        networkCurrency.code.equals(
            currencyCode,
            true
        )
    } != null

/** Returns the [Currency] code if the [Transfer] is a fee transfer in the wallet
 *  associated with [currencyId], blank otherwise. */
fun Transfer.feeForToken(currencyId: String): String =
    if (amount.currency.uids.equals(currencyId, true)) {
        ""
    } else {
        amount.currency.code
    }

fun WalletManagerState.isTracked() =
    type == WalletManagerState.Type.CONNECTED ||
        type == WalletManagerState.Type.SYNCING

/** Returns [Wallet] [Flow] sorted by [displayOrderCurrencyIds]. */
fun Flow<List<Wallet>>.applyDisplayOrder(displayOrderCurrencyIds: Flow<List<String>>) =
    combine(displayOrderCurrencyIds) { systemWallets, currencyIds ->
        currencyIds.mapNotNull {
            systemWallets.findByCurrencyId(it)
        }
    }

/** Returns the url scheme for a payment request with this wallet. */
val Wallet.urlScheme: String?
    get() = when {
        currency.code.isEthereum() || currency.isErc20() -> "ethereum"
        currency.code.isRipple() -> "xrp"
        currency.code.isBitcoin() -> "bitcoin"
        currency.code.isBitcoinCash() -> when {
            walletManager.network.isMainnet -> "bitcoincash"
            else -> "bchtest"
        }
        else -> null
    }

val Wallet.urlSchemes: List<String>
    get() = when {
        currency.code.isRipple() -> listOf(urlScheme!!, "xrpl", "ripple")
        else -> urlScheme?.run(::listOf) ?: emptyList()
    }

/** Return a [NetworkPeer] pointing to the given address */
fun Network.getPeerOrNull(node: String): NetworkPeer? {
    val nodeInfo = node.split(":")
    if (nodeInfo.isEmpty()) return null

    val address = nodeInfo[0]
    val port = if (nodeInfo.size > 1) {
        UnsignedInteger.valueOf(nodeInfo[1])
    } else {
        UnsignedInteger.valueOf(DEFAULT_PORT)
    }
    return createPeer(address, port, null).orNull()
}

/** True when the [Transfer] was received. */
fun Transfer.isReceived(): Boolean = direction == TransferDirection.RECEIVED

fun Transfer.getSize(): Double? {
    val currencyCode = wallet.currency.code
    return when {
        currencyCode.isBitcoin() || currencyCode.isBitcoinCash() ->
            (confirmedFeeBasis.orNull() ?: estimatedFeeBasis.orNull())?.costFactor
        else -> null
    }
}
