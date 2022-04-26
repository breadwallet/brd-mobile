/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import com.blockset.walletkit.Address
import com.blockset.walletkit.AddressScheme
import com.blockset.walletkit.errors.LimitEstimationError
import com.brd.exchange.WalletProvider
import com.breadwallet.BuildConfig
import com.breadwallet.breadbox.*
import com.breadwallet.logger.logError
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import kotlinx.coroutines.runBlocking
import java.util.*

class AndroidWalletProvider(
    private val breadBox: BreadBox,
    private val walletData: AccountMetaDataProvider,
) : WalletProvider {

    override fun loadWalletBalances(): Map<String, Double> {
        val wallets = breadBox.getSystemUnsafe()?.wallets.orEmpty()
        return wallets.associate { wallet ->
            val minBalance = wallet.balanceMinimum.orNull()
                ?.doubleAmount(wallet.defaultUnit)
                ?.orNull() ?: 0.0
            val walletBalance = wallet.balance.doubleAmount(wallet.defaultUnit).orNull() ?: 0.0
            Pair(
                wallet.currency.code.toLowerCase(Locale.ROOT),
                (walletBalance - minBalance).coerceAtLeast(0.0)
            )
        }
    }

    override fun enableWallet(currencyId: String) {
        walletData.enableWallet(currencyId.adjustId())
    }

    override fun receiveAddressFor(currencyId: String): String? {
        val adjustedCurrencyId = currencyId.adjustId()

        if (walletData.getEnabledWalletsUnsafe()?.contains(adjustedCurrencyId) == false) {
            enableWallet(adjustedCurrencyId)
        }
        val wallet = breadBox.getSystemUnsafe()
            ?.wallets
            ?.find { it.currency.uids == adjustedCurrencyId }
            ?: return null
        return if (wallet.currency.isBitcoin()) {
            wallet.getTargetForScheme(AddressScheme.BTC_LEGACY)
        } else {
            wallet.target
        }.toString()
    }

    override fun estimateLimitMaximum(currencyId: String, targetAddress: String): Double? {
        val wallet = breadBox.getSystemUnsafe()?.wallets?.find { it.currency.uids == currencyId } ?: return null
        val target = Address.create(targetAddress, wallet.walletManager.network).get()
        val fee = wallet.feeForSpeed(TransferSpeed.Priority(wallet.currency.code))
        return runBlocking {
            try {
                wallet.estimateMaximum(target, fee).doubleAmount(wallet.unit).orNull()
            } catch (e: LimitEstimationError) {
                logError("Failed to estimate max", e)
                null
            }
        }
    }

    override fun currencyCode(currencyId: String): String? {
        val wallet = breadBox.getSystemUnsafe()?.wallets?.find { it.currency.uids == currencyId }
        return wallet?.currency?.code
    }

    override fun networkCurrencyCode(currencyId: String): String? {
        val wallet = breadBox.getSystemUnsafe()?.wallets?.find { it.currency.uids == currencyId }
        return wallet?.walletManager?.network?.currency?.code
    }

    override fun estimateFee(currencyId: String, targetAddress: String): Double? {
        val wallet = breadBox.getSystemUnsafe()?.wallets?.find { it.currency.uids == currencyId } ?: return null
        val target = Address.create(targetAddress, wallet.walletManager.network).get()
        val fee = wallet.feeForSpeed(TransferSpeed.Priority(wallet.currency.code))
        return runBlocking {
            try {
                wallet.estimateFee(target, wallet.balance).fee.doubleAmount(wallet.unit).orNull()
            } catch (e: LimitEstimationError) {
                logError("Failed to estimate fee", e)
                null
            }
        }
    }

    private fun String.adjustId(): String = if (BuildConfig.BITCOIN_TESTNET) {
        replace("ethereum-mainnet", "ethereum-ropsten")
            .replace("mainnet", "testnet")
    } else this
}
