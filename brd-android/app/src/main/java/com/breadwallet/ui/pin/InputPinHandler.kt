/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 9/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.pin

import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.pin.InputPin.E
import com.breadwallet.ui.pin.InputPin.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createInputPinHandler(
    userManager: BrdUserManager
) = subtypeEffectHandler<F, E> {
    addFunction<F.SetupPin> { effect ->
        try {
            userManager.configurePinCode(effect.pin)
            E.OnPinSaved
        } catch (e: Exception) {
            E.OnPinSaveFailed
        }
    }

    addFunction<F.CheckIfPinExists> {
        E.OnPinCheck(userManager.hasPinCode())
    }

    addConsumer<F.TrackEvent> { (event) ->
        EventUtils.pushEvent(event)
    }
}

