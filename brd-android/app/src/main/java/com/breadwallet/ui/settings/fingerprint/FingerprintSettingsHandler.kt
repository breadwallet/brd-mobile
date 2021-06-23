/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.E
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createFingerprintSettingsHandler() = subtypeEffectHandler<F, E> {
    addFunction<F.LoadCurrentSettings> {
        E.OnSettingsLoaded(
            sendMoney = BRSharedPrefs.sendMoneyWithFingerprint,
            unlockApp = BRSharedPrefs.unlockWithFingerprint
        )
    }
    addConsumer<F.UpdateFingerprintSetting> { effect ->
        BRSharedPrefs.unlockWithFingerprint = effect.unlockApp
        BRSharedPrefs.sendMoneyWithFingerprint = effect.sendMoney
    }
}
