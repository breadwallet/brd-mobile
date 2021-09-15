/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.login

import android.content.Context
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.isFingerPrintAvailableAndSetup
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.login.LoginScreen.E
import com.breadwallet.ui.login.LoginScreen.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createLoginScreenHandler(
    context: Context,
    brdUser: BrdUserManager
) = subtypeEffectHandler<F, E> {
    addAction<F.UnlockBrdUser> {
        brdUser.unlock()
    }

    addFunction<F.CheckFingerprintEnable> {
        E.OnFingerprintEnabled(
            enabled = isFingerPrintAvailableAndSetup(context) && BRSharedPrefs.unlockWithFingerprint
        )
    }

    addConsumer<F.TrackEvent> { effect ->
        EventUtils.pushEvent(effect.eventName, effect.attributes)
    }
}
