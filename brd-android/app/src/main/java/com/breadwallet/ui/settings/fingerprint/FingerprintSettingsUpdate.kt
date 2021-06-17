/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.E
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.F
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object FingerprintSettingsUpdate : Update<M, E, F>, FingerprintSettingsUpdateSpec {

    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onBackClicked(model: M)
        : Next<M, F> =
        Next.dispatch(
            setOf(F.GoBack)
        )

    override fun onFaqClicked(model: M)
        : Next<M, F> =
        Next.dispatch(
            setOf(F.GoToFaq)
        )

    override fun onAppUnlockChanged(
        model: M,
        event: E.OnAppUnlockChanged
    ): Next<M, F> {
        val updatedModel = model.copy(
            unlockApp = event.enable,
            sendMoneyEnable = event.enable,
            sendMoney = (event.enable && model.sendMoney)
        )
        return next(
            updatedModel,
            setOf(
                F.UpdateFingerprintSetting(
                    updatedModel.unlockApp,
                    updatedModel.sendMoney
                )
            )
        )
    }

    override fun onSendMoneyChanged(
        model: M,
        event: E.OnSendMoneyChanged
    ): Next<M, F> {
        return next(
            model.copy(sendMoney = event.enable),
            setOf(
                F.UpdateFingerprintSetting(
                    model.unlockApp,
                    event.enable
                )
            )
        )
    }

    override fun onSettingsLoaded(
        model: M,
        event: E.OnSettingsLoaded
    ): Next<M, F> {
        return next(
            model.copy(
                unlockApp = event.unlockApp,
                sendMoney = event.sendMoney,
                sendMoneyEnable = event.unlockApp
            )
        )
    }
}
