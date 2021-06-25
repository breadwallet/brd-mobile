/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.showkey

import com.breadwallet.ui.navigation.OnCompleteAction
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object ShowPaperKeyUpdate : Update<ShowPaperKey.M, ShowPaperKey.E, ShowPaperKey.F>,
    ShowPaperKeyUpdateSpec {

    override fun update(
        model: ShowPaperKey.M,
        event: ShowPaperKey.E
    ): Next<ShowPaperKey.M, ShowPaperKey.F> = patch(model, event)

    override fun onNextClicked(
        model: ShowPaperKey.M
    ): Next<ShowPaperKey.M, ShowPaperKey.F> {
        return if (model.currentWord == model.phrase.size - 1) {
            val effect: ShowPaperKey.F = if (model.onComplete == null) {
                if (model.phraseWroteDown) {
                    ShowPaperKey.F.GoBack
                } else {
                    ShowPaperKey.F.GoToPaperKeyProve(model.phrase, OnCompleteAction.GO_HOME)
                }
            } else {
                ShowPaperKey.F.GoToPaperKeyProve(model.phrase, model.onComplete)
            }
            dispatch(setOf<ShowPaperKey.F>(effect))
        } else {
            next(model.copy(currentWord = model.currentWord + 1))
        }
    }

    override fun onPreviousClicked(
        model: ShowPaperKey.M
    ): Next<ShowPaperKey.M, ShowPaperKey.F> {
        check(model.currentWord > 0)
        return next(model.copy(currentWord = model.currentWord - 1))
    }

    override fun onPageChanged(
        model: ShowPaperKey.M,
        event: ShowPaperKey.E.OnPageChanged
    ): Next<ShowPaperKey.M, ShowPaperKey.F> {
        return next(model.copy(currentWord = event.position))
    }

    override fun onCloseClicked(
        model: ShowPaperKey.M
    ): Next<ShowPaperKey.M, ShowPaperKey.F> {
        val effect = when (model.onComplete) {
            OnCompleteAction.GO_HOME -> ShowPaperKey.F.GoToHome
            OnCompleteAction.GO_TO_BUY -> ShowPaperKey.F.GoToBuy
            null -> ShowPaperKey.F.GoBack
        } as ShowPaperKey.F
        return dispatch(setOf(effect))
    }
}
