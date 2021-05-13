/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.blockset.walletkit.*
import com.blockset.walletkit.Unit
import com.blockset.walletkit.errors.AccountInitializationError
import com.blockset.walletkit.errors.ExportablePaperWalletError
import com.blockset.walletkit.errors.FeeEstimationError
import com.blockset.walletkit.errors.LimitEstimationError
import com.blockset.walletkit.errors.WalletSweeperError
import com.breadwallet.util.asyncApiCall
import java.math.BigDecimal
import java.math.RoundingMode

/** Returns the [Address] object for [address] from the [Wallet]'s [Network] */
fun Wallet.addressFor(address: String): Address? {
    return Address.create(address, walletManager.network).orNull()
}

/**
 * By default [WalletManager.getDefaultNetworkFee] is used for the [networkFee].
 */
suspend fun Wallet.estimateFee(
    address: Address,
    amount: Amount,
    networkFee: NetworkFee = walletManager.defaultNetworkFee,
    attrs: Set<TransferAttribute> = emptySet()
): TransferFeeBasis = asyncApiCall<TransferFeeBasis, FeeEstimationError> {
    estimateFee(address, amount, networkFee, attrs, this)
}

suspend fun WalletManager.createSweeper(wallet: Wallet, key: Key): WalletSweeper =
    asyncApiCall<WalletSweeper, WalletSweeperError> { createSweeper(wallet, key, this) }

suspend fun WalletSweeper.estimateFee(networkFee: NetworkFee): TransferFeeBasis =
    asyncApiCall<TransferFeeBasis, FeeEstimationError> { estimate(networkFee, this) }

suspend fun System.accountInitialize(
    account: Account,
    network: Network,
    create: Boolean
): ByteArray = asyncApiCall<ByteArray, AccountInitializationError> {
    accountInitialize(account, network, create, this)
}

suspend fun Wallet.estimateMaximum(
    address: Address,
    networkFee: NetworkFee
): Amount = asyncApiCall<Amount, LimitEstimationError> {
    estimateLimitMaximum(address, networkFee, this)
}

suspend fun WalletManager.createExportablePaperWallet(): ExportablePaperWallet =
    asyncApiCall<ExportablePaperWallet, ExportablePaperWalletError> {
        createExportablePaperWallet(this)
    }

/** Returns the [Amount] as a [BigDecimal]. */
fun Amount.toBigDecimal(
    unit: Unit = this.unit,
    roundingMode: RoundingMode = RoundingMode.HALF_EVEN
): BigDecimal {
    return BigDecimal(doubleAmount(unit).or(0.0))
        .setScale(unit.decimals.toInt(), roundingMode)
}

fun Currency.isNative() = type.equals("native", true)

fun Currency.isErc20() = type.equals("erc20", true)

fun Currency.isEthereum() =
    uids.equals("ethereum-mainnet:__native__", true) ||
        uids.equals("ethereum-ropsten:__native__", true)

fun Currency.isBitcoin() =
    uids.equals("bitcoin-mainnet:__native__", true) ||
        uids.equals("bitcoin-testnet:__native__", true)

fun Currency.isBitcoinCash() =
    uids.equals("bitcoincash-mainnet:__native__", true) ||
        uids.equals("bitcoincash-testnet:__native__", true)

fun Currency.isTezos() = uids.equals("tezos-mainnet:__native__", true)

/** Returns the default [Unit] for the [Wallet]'s [Network]. */
val Wallet.defaultUnit: Unit
    get() = walletManager.network.defaultUnitFor(currency).get()

/** Returns the base [Unit] for the [Wallet]'s [Network] */
val Wallet.baseUnit: Unit
    get() = walletManager.network.baseUnitFor(currency).get()

fun Wallet.feeForSpeed(speed: TransferSpeed): NetworkFee {
    if (currency.isTezos()) {
        return checkNotNull(
            walletManager.network.fees.minByOrNull {
                it.confirmationTimeInMilliseconds.toLong()
            }
        )
    }
    val fees = walletManager.network.fees
    return when (fees.size) {
        1 -> fees.single()
        else -> fees
            .filter { it.confirmationTimeInMilliseconds.toLong() <= speed.targetTime }
            .minByOrNull { fee ->
                speed.targetTime - fee.confirmationTimeInMilliseconds.toLong()
            }
            ?: fees.minByOrNull { it.confirmationTimeInMilliseconds }
            ?: walletManager.defaultNetworkFee
    }
}

val Wallet.isSyncing get() = walletManager.state == WalletManagerState.SYNCING()
