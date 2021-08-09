/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.provekey

import com.spotify.mobius.Next

interface PaperKeyProveUpdateSpec {
    fun patch(model: PaperKeyProve.M, event: PaperKeyProve.E): Next<PaperKeyProve.M, PaperKeyProve.F> = when (event) {
        PaperKeyProve.E.OnSubmitClicked -> onSubmitClicked(model)
        PaperKeyProve.E.OnBreadSignalShown -> onBreadSignalShown(model)
        PaperKeyProve.E.OnWroteDownKeySaved -> onWroteDownKeySaved(model)
        is PaperKeyProve.E.OnFirstWordChanged -> onFirstWordChanged(model, event)
        is PaperKeyProve.E.OnSecondWordChanged -> onSecondWordChanged(model, event)
    }

    fun onSubmitClicked(model: PaperKeyProve.M): Next<PaperKeyProve.M, PaperKeyProve.F>

    fun onBreadSignalShown(model: PaperKeyProve.M): Next<PaperKeyProve.M, PaperKeyProve.F>

    fun onWroteDownKeySaved(model: PaperKeyProve.M): Next<PaperKeyProve.M, PaperKeyProve.F>

    fun onFirstWordChanged(model: PaperKeyProve.M, event: PaperKeyProve.E.OnFirstWordChanged): Next<PaperKeyProve.M, PaperKeyProve.F>

    fun onSecondWordChanged(model: PaperKeyProve.M, event: PaperKeyProve.E.OnSecondWordChanged): Next<PaperKeyProve.M, PaperKeyProve.F>
}
