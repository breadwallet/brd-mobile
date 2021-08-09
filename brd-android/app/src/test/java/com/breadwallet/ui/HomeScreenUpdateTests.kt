/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 11/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui

import com.breadwallet.ui.home.HomeScreen.E
import com.breadwallet.ui.home.HomeScreen.F
import com.breadwallet.ui.home.HomeScreen.M
import com.breadwallet.ui.home.HomeScreenUpdate
import com.breadwallet.ui.home.PromptItem
import com.breadwallet.ui.home.Wallet
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

@Suppress("LongMethod")
class HomeScreenUpdateTests {

    private val WALLET_BITCOIN = Wallet("bitcoin-mainnet:__native__", "Bitcoin", "btc")
    private val WALLET_ETHEREUM = Wallet("ethereum-mainnet:__native__", "Ethereum", "eth")

    private val spec = UpdateSpec(HomeScreenUpdate)

    @Test
    fun addAndUpdateWallets() {

        val initState = M.createDefault()

        // Add initial list of wallets
        val wallets = mapOf(WALLET_BITCOIN.currencyCode to WALLET_BITCOIN.copy())

        spec.given(initState)
            .`when`(
                E.OnEnabledWalletsUpdated(wallets.values.toList())
            )
            .then(
                assertThatNext(
                    hasModel(
                        initState.copy(
                            wallets = wallets,
                            displayOrder = wallets.values.map { it.currencyId }
                        )
                    ),
                    hasEffects<M, F>(F.LoadWallets)
                )
            )

        spec.given(initState)
            .`when`(
                E.OnWalletsUpdated(wallets.values.toList())
            )
            .then(
                assertThatNext(
                    hasModel(
                        initState.copy(
                            wallets = wallets
                        )
                    ),
                    hasNoEffects()
                )
            )

        // Add ETH wallet
        val initialWalletsAddedState = initState.copy(wallets = wallets)
        val walletToAdd = WALLET_ETHEREUM.copy()

        var expectedWallets = wallets.toMutableMap()
        expectedWallets[walletToAdd.currencyCode] = walletToAdd

        spec.given(initialWalletsAddedState)
            .`when`(
                E.OnWalletsUpdated(expectedWallets.values.toList())
            )
            .then(
                assertThatNext(
                    hasModel(
                        initialWalletsAddedState.copy(
                            wallets = expectedWallets
                        )
                    ),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun connectivityUpdate() {
        val initState = M.createDefault()

        spec.given(initState)
            .`when`(
                E.OnConnectionUpdated(isConnected = false)
            )
            .then(
                assertThatNext(
                    hasModel(initState.copy(hasInternet = false)),
                    hasNoEffects()
                )
            )
    }

    @Test
    fun walletClick() {
        val initState = M.createDefault()
            .copy(wallets = mutableMapOf(WALLET_BITCOIN.currencyCode to WALLET_BITCOIN.copy(state = Wallet.State.LOADING)))

        spec.given(initState)
            .`when`(
                E.OnWalletClicked(currencyCode = WALLET_BITCOIN.currencyCode)
            )
            .then(
                assertThatNext(
                    hasNoEffects()
                )
            )

        val initializedWallet = WALLET_BITCOIN.copy(state = Wallet.State.READY)
        val updatedState =
            initState.copy(wallets = mutableMapOf(initializedWallet.currencyCode to initializedWallet))
        val expectedEffect = F.GoToWallet(WALLET_BITCOIN.currencyCode)

        spec.given(updatedState)
            .`when`(
                E.OnWalletClicked(currencyCode = initializedWallet.currencyCode)
            )
            .then(
                assertThatNext(
                    hasEffects(expectedEffect as F)
                )
            )
    }

    @Test
    fun buyClick() {
        val initState = M.createDefault()

        spec.given(initState)
            .`when`(
                E.OnBuyClicked
            )
            .then(
                assertThatNext(
                    hasEffects(F.GoToBuy as F)
                )
            )
    }

    @Test
    fun tradeClick() {
        val initState = M.createDefault()

        spec.given(initState)
            .`when`(
                E.OnTradeClicked
            )
            .then(
                assertThatNext(
                    hasEffects(F.GoToTrade as F)
                )
            )
    }

    @Test
    fun menuClick() {
        val initState = M.createDefault()

        spec.given(initState)
            .`when`(
                E.OnMenuClicked
            )
            .then(
                assertThatNext(
                    hasEffects(F.GoToMenu as F)
                )
            )
    }

    @Test
    fun promptLoaded() {
        val initState = M.createDefault()

        spec.given(initState)
            .`when`(
                E.OnPromptLoaded(promptId = PromptItem.EMAIL_COLLECTION)
            )
            .then(
                assertThatNext(
                    hasModel(initState.copy(promptId = PromptItem.EMAIL_COLLECTION))
                )
            )
    }
}
