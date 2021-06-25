/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.login

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.login.LoginScreen.E
import com.breadwallet.ui.login.LoginScreen.F
import com.breadwallet.ui.login.LoginScreen.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object LoginUpdate : Update<M, E, F>, LoginScreenUpdateSpec {

    override fun update(model: M, event: E): Next<M, F> =
        patch(model, event)

    override fun onFingerprintClicked(model: M): Next<M, F> =
        dispatch(setOf(F.ShowFingerprintController))

    override fun onAuthenticationSuccess(model: M): Next<M, F> =
        next(
            model.copy(isUnlocked = true),
            setOf(
                F.AuthenticationSuccess,
                F.UnlockBrdUser,
                F.TrackEvent(EventUtils.EVENT_LOGIN_SUCCESS)
            )
        )

    override fun onAuthenticationFailed(model: M): Next<M, F> =
        dispatch(
            setOf(
                F.AuthenticationFailed,
                F.TrackEvent(EventUtils.EVENT_LOGIN_FAILED)
            )
        )

    override fun onPinLocked(model: M): Next<M, F> =
        dispatch(setOf(F.GoToDisableScreen))

    override fun onUnlockAnimationEnd(model: M): Next<M, F> {
        val effect = when {
            model.extraUrl.isNotBlank() ->
                F.GoToDeepLink(model.extraUrl)
            model.showHomeScreen -> F.GoToHome
            else -> F.GoBack
        } as F
        return dispatch(setOf(effect))
    }

    override fun onFingerprintEnabled(
        model: M,
        event: E.OnFingerprintEnabled
    ): Next<M, F> = next(
        model.copy(fingerprintEnable = event.enabled),
        if (event.enabled) {
            setOf(F.ShowFingerprintController)
        } else {
            emptySet()
        }
    )
}
