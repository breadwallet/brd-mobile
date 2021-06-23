/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.pin

import com.spotify.mobius.Next

interface InputPinUpdateSpec {
    fun patch(model: InputPin.M, event: InputPin.E): Next<InputPin.M, InputPin.F> = when (event) {
        InputPin.E.OnFaqClicked -> onFaqClicked(model)
        InputPin.E.OnPinLocked -> onPinLocked(model)
        InputPin.E.OnPinSaved -> onPinSaved(model)
        InputPin.E.OnPinSaveFailed -> onPinSaveFailed(model)
        is InputPin.E.OnPinEntered -> onPinEntered(model, event)
        is InputPin.E.OnPinCheck -> onPinCheck(model, event)
    }

    fun onFaqClicked(model: InputPin.M): Next<InputPin.M, InputPin.F>

    fun onPinLocked(model: InputPin.M): Next<InputPin.M, InputPin.F>

    fun onPinSaved(model: InputPin.M): Next<InputPin.M, InputPin.F>

    fun onPinSaveFailed(model: InputPin.M): Next<InputPin.M, InputPin.F>

    fun onPinEntered(model: InputPin.M, event: InputPin.E.OnPinEntered): Next<InputPin.M, InputPin.F>

    fun onPinCheck(model: InputPin.M, event: InputPin.E.OnPinCheck): Next<InputPin.M, InputPin.F>
}