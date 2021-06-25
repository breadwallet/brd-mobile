/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.receive

import com.spotify.mobius.Next

interface ReceiveScreenUpdateSpec {
    fun patch(model: ReceiveScreen.M, event: ReceiveScreen.E): Next<ReceiveScreen.M, ReceiveScreen.F> = when (event) {
        ReceiveScreen.E.OnCloseClicked -> onCloseClicked(model)
        ReceiveScreen.E.OnFaqClicked -> onFaqClicked(model)
        ReceiveScreen.E.OnShareClicked -> onShareClicked(model)
        ReceiveScreen.E.OnCopyAddressClicked -> onCopyAddressClicked(model)
        ReceiveScreen.E.OnAmountClicked -> onAmountClicked(model)
        ReceiveScreen.E.OnToggleCurrencyClicked -> onToggleCurrencyClicked(model)
        is ReceiveScreen.E.OnExchangeRateUpdated -> onExchangeRateUpdated(model, event)
        is ReceiveScreen.E.OnWalletInfoLoaded -> onWalletInfoLoaded(model, event)
        is ReceiveScreen.E.OnAmountChange -> onAmountChange(model, event)
    }

    fun onCloseClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onFaqClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onShareClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onCopyAddressClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onAmountClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onToggleCurrencyClicked(model: ReceiveScreen.M): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onExchangeRateUpdated(model: ReceiveScreen.M, event: ReceiveScreen.E.OnExchangeRateUpdated): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onWalletInfoLoaded(model: ReceiveScreen.M, event: ReceiveScreen.E.OnWalletInfoLoaded): Next<ReceiveScreen.M, ReceiveScreen.F>

    fun onAmountChange(model: ReceiveScreen.M, event: ReceiveScreen.E.OnAmountChange): Next<ReceiveScreen.M, ReceiveScreen.F>
}