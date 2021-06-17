/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.writedownkey

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.navigation.OnCompleteAction
import com.breadwallet.ui.writedownkey.WriteDownKey.E
import com.breadwallet.ui.writedownkey.WriteDownKey.F
import com.breadwallet.ui.writedownkey.WriteDownKey.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

object WriteDownKeyUpdate : Update<M, E, F>, WriteDownKeyUpdateSpec {

    override fun update(model: M, event: E) = patch(model, event)

    override fun onCloseClicked(model: M): Next<M, F> {
        val effect = when (model.onComplete) {
            OnCompleteAction.GO_TO_BUY -> F.GoToBuy
            OnCompleteAction.GO_HOME -> F.GoToHome
        } as F
        return dispatch(
            setOf(
                effect,
                F.TrackEvent(EventUtils.EVENT_PAPER_KEY_INTRO_DISMISSED),
                F.TrackEvent(EventUtils.EVENT_ONBOARDING_COMPLETE)
            )
        )
    }

    override fun onFaqClicked(model: M): Next<M, F> =
        dispatch(setOf(F.GoToFaq))

    override fun onWriteDownClicked(model: M): Next<M, F> =
        dispatch(
            setOf(
                when {
                    model.requestAuth -> F.ShowAuthPrompt
                    else -> F.GetPhrase
                },
                F.TrackEvent(EventUtils.EVENT_PAPER_KEY_INTRO_GENEREATE_KEY)
            )
        )

    override fun onGetPhraseFailed(model: M): Next<M, F> =
        noChange()

    override fun onUserAuthenticated(model: M): Next<M, F> =
        dispatch(setOf(F.GetPhrase))

    override fun onPhraseRecovered(
        model: M,
        event: E.OnPhraseRecovered
    ): Next<M, F> =
        dispatch(setOf(F.GoToPaperKey(event.phrase, model.onComplete)))
}
