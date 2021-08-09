/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.txdetails

import com.spotify.mobius.Next

interface TxDetailsUpdateSpec {
    fun patch(model: TxDetails.M, event: TxDetails.E): Next<TxDetails.M, TxDetails.F> = when (event) {
        TxDetails.E.OnTransactionHashClicked -> onTransactionHashClicked(model)
        TxDetails.E.OnAddressClicked -> onAddressClicked(model)
        TxDetails.E.OnClosedClicked -> onClosedClicked(model)
        TxDetails.E.OnShowHideDetailsClicked -> onShowHideDetailsClicked(model)
        TxDetails.E.OnGiftResendClicked -> onGiftResendClicked(model)
        TxDetails.E.OnGiftReclaimClicked -> onGiftReclaimClicked(model)
        is TxDetails.E.OnTransactionUpdated -> onTransactionUpdated(model, event)
        is TxDetails.E.OnFiatAmountNowUpdated -> onFiatAmountNowUpdated(model, event)
        is TxDetails.E.OnMetaDataUpdated -> onMetaDataUpdated(model, event)
        is TxDetails.E.OnMemoChanged -> onMemoChanged(model, event)
    }

    fun onTransactionHashClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>

    fun onAddressClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>

    fun onClosedClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>

    fun onShowHideDetailsClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>

    fun onTransactionUpdated(model: TxDetails.M, event: TxDetails.E.OnTransactionUpdated): Next<TxDetails.M, TxDetails.F>

    fun onFiatAmountNowUpdated(model: TxDetails.M, event: TxDetails.E.OnFiatAmountNowUpdated): Next<TxDetails.M, TxDetails.F>

    fun onMetaDataUpdated(model: TxDetails.M, event: TxDetails.E.OnMetaDataUpdated): Next<TxDetails.M, TxDetails.F>

    fun onMemoChanged(model: TxDetails.M, event: TxDetails.E.OnMemoChanged): Next<TxDetails.M, TxDetails.F>

    fun onGiftResendClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>

    fun onGiftReclaimClicked(model: TxDetails.M): Next<TxDetails.M, TxDetails.F>
}
