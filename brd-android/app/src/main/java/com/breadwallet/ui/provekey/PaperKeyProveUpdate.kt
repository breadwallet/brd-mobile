/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.provekey

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.navigation.OnCompleteAction
import com.breadwallet.ui.provekey.PaperKeyProve.E
import com.breadwallet.ui.provekey.PaperKeyProve.F
import com.breadwallet.ui.provekey.PaperKeyProve.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object PaperKeyProveUpdate : Update<M, E, F>,
    PaperKeyProveUpdateSpec {
    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onSubmitClicked(model: M)
        : Next<M, F> {
        return dispatch(
            setOf(
                if (model.firstWordState == M.WordState.VALID
                    && model.secondWordSate == M.WordState.VALID
                ) {
                    F.StoreWroteDownPhrase
                } else {
                    F.ShakeWords(
                        model.firstWordState != M.WordState.VALID,
                        model.secondWordSate != M.WordState.VALID
                    )
                }
            )
        )
    }

    override fun onFirstWordChanged(
        model: M,
        event: E.OnFirstWordChanged
    ): Next<M, F> {
        val state = when {
            event.word.isEmpty() -> M.WordState.EMPTY
            event.word == model.firstWord -> M.WordState.VALID
            else -> M.WordState.INVALID
        }
        return next(model.copy(firstWordState = state))
    }

    override fun onSecondWordChanged(
        model: M,
        event: E.OnSecondWordChanged
    ): Next<M, F> {
        val state = when {
            event.word.isEmpty() -> M.WordState.EMPTY
            event.word == model.secondWord -> M.WordState.VALID
            else -> M.WordState.INVALID
        }
        return next(model.copy(secondWordSate = state))
    }

    override fun onBreadSignalShown(model: M): Next<M, F> =
        dispatch(
            setOf(
                when (model.onComplete) {
                    OnCompleteAction.GO_TO_BUY -> F.GoToBuy
                    OnCompleteAction.GO_HOME -> F.GoToHome
                } as F,
                F.TrackEvent(EventUtils.EVENT_ONBOARDING_COMPLETE)
            )
        )

    override fun onWroteDownKeySaved(model: M): Next<M, F> =
        next(model.copy(showBreadSignal = true), setOf(F.ShowStoredSignal))
}
