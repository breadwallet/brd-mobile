/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.addwallets

import com.breadwallet.ui.addwallets.AddWallets.E
import com.breadwallet.ui.addwallets.AddWallets.F
import com.breadwallet.ui.addwallets.AddWallets.M
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object AddWalletsUpdate : Update<M, E, F>, AddWalletsUpdateSpec {

    override fun update(model: M, event: E) = patch(model, event)

    override fun onBackClicked(model: M): Next<M, F> {
        return dispatch(
            effects(
                F.GoBack
            )
        )
    }

    override fun onSearchQueryChanged(
        model: M,
        event: E.OnSearchQueryChanged
    ): Next<M, F> {
        return next(
            model.copy(searchQuery = event.query),
            effects(
                F.SearchTokens(event.query)
            )
        )
    }

    override fun onTokensChanged(
        model: M,
        event: E.OnTokensChanged
    ): Next<M, F> {
        return next(
            model.copy(
                tokens = event.tokens
            )
        )
    }

    override fun onAddWalletClicked(
        model: M,
        event: E.OnAddWalletClicked
    ): Next<M, F> {
        return dispatch(
            effects(
                F.AddWallet(event.token)
            )
        )
    }

    override fun onRemoveWalletClicked(
        model: M,
        event: E.OnRemoveWalletClicked
    ): Next<M, F> {
        return dispatch(
            effects(
                F.RemoveWallet(event.token)
            )
        )
    }
}
