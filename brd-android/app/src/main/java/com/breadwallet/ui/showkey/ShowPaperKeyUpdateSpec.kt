/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.showkey

import com.spotify.mobius.Next

interface ShowPaperKeyUpdateSpec {
    fun patch(model: ShowPaperKey.M, event: ShowPaperKey.E): Next<ShowPaperKey.M, ShowPaperKey.F> = when (event) {
        ShowPaperKey.E.OnNextClicked -> onNextClicked(model)
        ShowPaperKey.E.OnPreviousClicked -> onPreviousClicked(model)
        ShowPaperKey.E.OnCloseClicked -> onCloseClicked(model)
        is ShowPaperKey.E.OnPageChanged -> onPageChanged(model, event)
    }

    fun onNextClicked(model: ShowPaperKey.M): Next<ShowPaperKey.M, ShowPaperKey.F>

    fun onPreviousClicked(model: ShowPaperKey.M): Next<ShowPaperKey.M, ShowPaperKey.F>

    fun onCloseClicked(model: ShowPaperKey.M): Next<ShowPaperKey.M, ShowPaperKey.F>

    fun onPageChanged(model: ShowPaperKey.M, event: ShowPaperKey.E.OnPageChanged): Next<ShowPaperKey.M, ShowPaperKey.F>
}
