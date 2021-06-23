/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.breadwallet.ui.settings.segwit.EnableSegWit.E
import com.breadwallet.ui.settings.segwit.EnableSegWit.F
import com.breadwallet.ui.settings.segwit.EnableSegWit.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object EnableSegWitUpdate : Update<M, E, F>, EnableSegWitUpdateSpec {
    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onEnableClick(model: M): Next<M, F> =
        next(model.copy(state = M.State.CONFIRMATION))

    override fun onContinueClicked(model: M): Next<M, F> =
        next(
            model.copy(state = M.State.DONE),
            setOf(F.EnableSegWit)
        )

    override fun onCancelClicked(model: M): Next<M, F> =
        next(model.copy(state = M.State.ENABLE))

    override fun onBackClicked(model: M): Next<M, F> =
        dispatch(setOf(F.GoBack))

    override fun onDoneClicked(model: M): Next<M, F> =
        dispatch(setOf(F.GoToHome))
}
