/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.containsCurrency
import com.breadwallet.breadbox.currencyId
import com.breadwallet.breadbox.getPeerOrNull
import com.breadwallet.logger.logError
import com.breadwallet.mobius.bindConsumerIn
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.util.btc
import com.breadwallet.ui.settings.nodeselector.NodeSelector.E
import com.breadwallet.ui.settings.nodeselector.NodeSelector.F
import com.breadwallet.util.errorHandler
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

class NodeSelectorHandler(
    private val output: Consumer<E>,
    private val breadBox: BreadBox
) : Connection<F>,
    CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.Default + errorHandler()

    init {
        breadBox.wallet(btc)
            .map { it.walletManager.state }
            .distinctUntilChanged()
            .map { E.OnConnectionStateUpdated(it) }
            .bindConsumerIn(output, this)
    }

    override fun accept(effect: F) {
        when (effect) {
            F.LoadConnectionInfo -> loadConnectionInfo()
            F.SetToAutomatic -> setToAutomatic()
            is F.SetCustomNode -> setToManual(effect)
        }
    }

    override fun dispose() {
        coroutineContext.cancel()
    }

    private fun loadConnectionInfo() {
        val node = BRSharedPrefs.getTrustNode(iso = btc.toUpperCase(Locale.ROOT))
        val event = if (node.isNullOrBlank()) {
            E.OnConnectionInfoLoaded(NodeSelector.Mode.AUTOMATIC)
        } else {
            E.OnConnectionInfoLoaded(NodeSelector.Mode.MANUAL, node)
        }
        output.accept(event)
    }

    private fun setToAutomatic() {
        BRSharedPrefs.putTrustNode(
            iso = btc.toUpperCase(Locale.ROOT),
            trustNode = ""
        )
        launch {
            val walletManager = breadBox.wallet(btc).first().walletManager
            walletManager.connect(null)
        }
        output.accept(E.OnConnectionInfoLoaded(NodeSelector.Mode.AUTOMATIC))
    }

    private fun setToManual(effect: F.SetCustomNode) {
        BRSharedPrefs.putTrustNode(
            iso = btc.toUpperCase(Locale.ROOT),
            trustNode = effect.node
        )
        launch {
            val wallet = breadBox
                .wallet(btc)
                .first()
            val system = checkNotNull(breadBox.getSystemUnsafe())
            val network = system.networks.find { it.containsCurrency(wallet.currencyId) }

            val networkPeer = network?.getPeerOrNull(effect.node)
            if (networkPeer != null) {
                wallet.walletManager.connect(networkPeer)
                output.accept(E.OnConnectionInfoLoaded(NodeSelector.Mode.MANUAL, effect.node))
            } else {
                logError("Failed to create network peer")
            }
        }
    }
}
