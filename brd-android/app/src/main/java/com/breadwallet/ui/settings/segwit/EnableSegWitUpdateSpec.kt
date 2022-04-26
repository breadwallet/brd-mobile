/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.spotify.mobius.Next

interface EnableSegWitUpdateSpec {
    fun patch(model: EnableSegWit.M, event: EnableSegWit.E): Next<EnableSegWit.M, EnableSegWit.F> = when (event) {
        EnableSegWit.E.OnEnableClick -> onEnableClick(model)
        EnableSegWit.E.OnContinueClicked -> onContinueClicked(model)
        EnableSegWit.E.OnCancelClicked -> onCancelClicked(model)
        EnableSegWit.E.OnBackClicked -> onBackClicked(model)
        EnableSegWit.E.OnDoneClicked -> onDoneClicked(model)
    }

    fun onEnableClick(model: EnableSegWit.M): Next<EnableSegWit.M, EnableSegWit.F>

    fun onContinueClicked(model: EnableSegWit.M): Next<EnableSegWit.M, EnableSegWit.F>

    fun onCancelClicked(model: EnableSegWit.M): Next<EnableSegWit.M, EnableSegWit.F>

    fun onBackClicked(model: EnableSegWit.M): Next<EnableSegWit.M, EnableSegWit.F>

    fun onDoneClicked(model: EnableSegWit.M): Next<EnableSegWit.M, EnableSegWit.F>
}
