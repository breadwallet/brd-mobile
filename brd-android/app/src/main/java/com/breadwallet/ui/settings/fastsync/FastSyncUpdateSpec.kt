/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fastsync

import com.spotify.mobius.Next

interface FastSyncUpdateSpec {
    fun patch(model: FastSync.M, event: FastSync.E): Next<FastSync.M, FastSync.F> = when (event) {
        FastSync.E.OnBackClicked -> onBackClicked(model)
        FastSync.E.OnLearnMoreClicked -> onLearnMoreClicked(model)
        FastSync.E.OnDisableFastSyncConfirmed -> onDisableFastSyncConfirmed(model)
        FastSync.E.OnDisableFastSyncCanceled -> onDisableFastSyncCanceled(model)
        is FastSync.E.OnFastSyncChanged -> onFastSyncChanged(model, event)
        is FastSync.E.OnSyncModesUpdated -> onSyncModesUpdated(model, event)
        is FastSync.E.OnCurrencyIdsUpdated -> onCurrencyIdsUpdated(model, event)
    }

    fun onBackClicked(model: FastSync.M): Next<FastSync.M, FastSync.F>

    fun onLearnMoreClicked(model: FastSync.M): Next<FastSync.M, FastSync.F>

    fun onDisableFastSyncConfirmed(model: FastSync.M): Next<FastSync.M, FastSync.F>

    fun onDisableFastSyncCanceled(model: FastSync.M): Next<FastSync.M, FastSync.F>

    fun onFastSyncChanged(model: FastSync.M, event: FastSync.E.OnFastSyncChanged): Next<FastSync.M, FastSync.F>

    fun onSyncModesUpdated(model: FastSync.M, event: FastSync.E.OnSyncModesUpdated): Next<FastSync.M, FastSync.F>

    fun onCurrencyIdsUpdated(model: FastSync.M, event: FastSync.E.OnCurrencyIdsUpdated): Next<FastSync.M, FastSync.F>
}