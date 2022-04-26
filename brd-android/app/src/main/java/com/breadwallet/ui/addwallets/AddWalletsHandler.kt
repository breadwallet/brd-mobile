/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.addwallets

import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.containsCurrency
import com.breadwallet.breadbox.currencyId
import com.breadwallet.breadbox.findByCurrencyId
import com.breadwallet.breadbox.findCurrency
import com.breadwallet.breadbox.isNative
import com.breadwallet.breadbox.networkContainsCurrency
import com.blockset.walletkit.Wallet
import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logError
import com.breadwallet.model.TokenItem
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.ui.addwallets.AddWallets.E
import com.breadwallet.ui.addwallets.AddWallets.F
import drewcarlson.mobius.flow.flowTransformer
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

fun createAddWalletsHandler(
    breadBox: BreadBox,
    acctMetaDataProvider: AccountMetaDataProvider
) = subtypeEffectHandler<F, E> {
    addTransformer(searchTokens(breadBox, acctMetaDataProvider))
    addConsumer(addWallet(breadBox, acctMetaDataProvider))
    addConsumer(removeWallet(acctMetaDataProvider))
}

fun List<String>.toTokenItems(): List<TokenItem> {
    return this.mapNotNull {
        TokenUtil.tokenForCurrencyId(it)
    }
}

private fun searchTokens(
    breadBox: BreadBox,
    acctMetaDataProvider: AccountMetaDataProvider
) = flowTransformer<F.SearchTokens, E> { effects ->
    effects
        .flatMapLatest { effect ->
            acctMetaDataProvider.enabledWallets().map { enabledWallets ->
                enabledWallets to effect.query
            }
        }
        .mapLatest { (trackedWallets, query) ->
            val system = breadBox.system().first()
            val availableWallets = system.wallets
            TokenUtil.getTokenItems()
                .filter { token ->
                    val isAlreadyAdded = trackedWallets.any { it == token.currencyId }
                    isAlreadyAdded || token.isSupported
                }
                .applyFilter(query)
                .map { tokenItem ->
                    val currencyId = tokenItem.currencyId
                    tokenItem.asToken(
                        enabled = trackedWallets.contains(currencyId),
                        removable = isRemovable(
                            availableWallets.findByCurrencyId(currencyId),
                            trackedWallets
                        )
                    )
                }
        }
        .map { tokens -> E.OnTokensChanged(tokens) }
}

/** Adds a [Wallet] for the given [currencyId] and its native wallet, if not already tracked. */
private fun addWallet(
    breadBox: BreadBox,
    acctMetaDataProvider: AccountMetaDataProvider
): suspend (F.AddWallet) -> Unit = { addWallet ->
    val currencyId = addWallet.token.currencyId
    val system = checkNotNull(breadBox.getSystemUnsafe())
    val network = system.networks.find { it.containsCurrency(currencyId) }

    when (network?.findCurrency(currencyId)?.isNative()) {
        null -> logError("No network or currency found for $currencyId.")
        false -> {
            val trackedWallets = breadBox.wallets().first()
            if (!trackedWallets.containsCurrency(network.currency.uids)) {
                logDebug("Adding native wallet ${network.currency.uids} for $currencyId.")
                acctMetaDataProvider.enableWallet(network.currency.uids)
            }
        }
    }

    logDebug("Adding wallet '$currencyId'")
    acctMetaDataProvider.enableWallet(currencyId)
}

private fun removeWallet(
    acctMetaDataProvider: AccountMetaDataProvider
): suspend (F.RemoveWallet) -> Unit = { removeWallet ->
    val currencyId = removeWallet.token.currencyId
    logDebug("Removing wallet '$currencyId'")
    acctMetaDataProvider.disableWallet(currencyId)
}

/**
 * Returns true if the [Wallet] exists, it's not the last remaining enabled [Wallet], and
 * it's not a [Wallet] another enabled [Wallet] depends on.
 */
private fun isRemovable(wallet: Wallet?, trackedWallets: List<String>) =
    trackedWallets.size > 1 && (wallet?.let { !walletIsNeeded(it, trackedWallets) } ?: true)

private fun walletIsNeeded(wallet: Wallet, trackedWallets: List<String>) =
    wallet.currency.isNative() &&
        trackedWallets.filter { !it.equals(wallet.currencyId, true) }
            .any {
                wallet.walletManager.networkContainsCurrency(it)
            }

private fun List<TokenItem>.applyFilter(query: String) =
    filter { token ->
        (
            token.name.contains(query, true) ||
                token.symbol.contains(query, true)
            )
    }

private fun TokenItem.asToken(enabled: Boolean, removable: Boolean): Token {
    return Token(
        name,
        symbol,
        checkNotNull(currencyId),
        checkNotNull(startColor),
        enabled,
        removable
    )
}
