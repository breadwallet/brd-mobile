/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.sync

import com.breadwallet.ui.sync.SyncBlockchain.E
import com.breadwallet.ui.sync.SyncBlockchain.F
import com.breadwallet.ui.sync.SyncBlockchain.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Update

object SyncBlockchainUpdate : Update<M, E, F> {
    override fun update(model: M, event: E): Next<M, F> =
        when (event) {
            E.OnFaqClicked -> dispatch(setOf(F.Nav.GoToSyncFaq(model.currencyCode)))
            E.OnSyncClicked -> dispatch(setOf(F.Nav.ShowSyncConfirmation))
            E.OnConfirmSyncClicked -> dispatch(setOf(F.SyncBlockchain(model.currencyCode)))
            E.OnSyncStarted -> dispatch(setOf(F.Nav.GoToHome))
        }
}
