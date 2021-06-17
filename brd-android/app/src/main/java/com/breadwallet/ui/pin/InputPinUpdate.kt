/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 9/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.pin

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.pin.InputPin.E
import com.breadwallet.ui.pin.InputPin.F
import com.breadwallet.ui.pin.InputPin.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object InputPinUpdate : Update<M, E, F>, InputPinUpdateSpec {

    override fun update(
        model: M,
        event: E
    ) = patch(model, event)

    override fun onFaqClicked(model: M): Next<M, F> =
        Next.dispatch(setOf(F.GoToFaq))

    override fun onPinEntered(
        model: M,
        event: E.OnPinEntered
    ): Next<M, F> {
        return when (model.mode) {
            M.Mode.VERIFY -> if (event.isPinCorrect) {
                next(model.copy(pinUpdateMode = true, mode = M.Mode.NEW))
            } else {
                next(model, setOf<F>(F.ErrorShake))
            }
            M.Mode.NEW -> {
                next(model.copy(mode = M.Mode.CONFIRM, pin = event.pin))
            }
            M.Mode.CONFIRM -> if (event.pin == model.pin) {
                next(model, setOf<F>(F.SetupPin(model.pin)))
            } else {
                next(
                    model.copy(mode = M.Mode.NEW, pin = ""),
                    setOf<F>(F.ErrorShake)
                )
            }
        }
    }

    override fun onPinLocked(model: M): Next<M, F> =
        next(model, setOf(F.GoToDisabledScreen))

    override fun onPinSaved(model: M): Next<M, F> {
        val effects = if (model.pinUpdateMode || model.skipWriteDownKey) {
            setOf(F.GoToHome)
        } else {
            setOf(
                F.GoToWriteDownKey(model.onComplete),
                F.TrackEvent(EventUtils.EVENT_ONBOARDING_PIN_CREATED)
            )
        }
        return next(model, effects)
    }

    override fun onPinSaveFailed(model: M): Next<M, F> {
        return next(model.copy(mode = M.Mode.NEW, pin = ""), setOf<F>(F.ShowPinError))
    }

    override fun onPinCheck(
        model: M,
        event: E.OnPinCheck
    ): Next<M, F> {
        val mode = if (event.hasPin) M.Mode.VERIFY else M.Mode.NEW
        return next(model.copy(mode = mode))
    }
}
