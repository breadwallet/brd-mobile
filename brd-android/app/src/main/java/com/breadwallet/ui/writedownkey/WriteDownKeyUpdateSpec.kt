/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.writedownkey

import com.spotify.mobius.Next

interface WriteDownKeyUpdateSpec {
    fun patch(model: WriteDownKey.M, event: WriteDownKey.E): Next<WriteDownKey.M, WriteDownKey.F> = when (event) {
        WriteDownKey.E.OnCloseClicked -> onCloseClicked(model)
        WriteDownKey.E.OnFaqClicked -> onFaqClicked(model)
        WriteDownKey.E.OnWriteDownClicked -> onWriteDownClicked(model)
        WriteDownKey.E.OnGetPhraseFailed -> onGetPhraseFailed(model)
        WriteDownKey.E.OnUserAuthenticated -> onUserAuthenticated(model)
        is WriteDownKey.E.OnPhraseRecovered -> onPhraseRecovered(model, event)
    }

    fun onCloseClicked(model: WriteDownKey.M): Next<WriteDownKey.M, WriteDownKey.F>

    fun onFaqClicked(model: WriteDownKey.M): Next<WriteDownKey.M, WriteDownKey.F>

    fun onWriteDownClicked(model: WriteDownKey.M): Next<WriteDownKey.M, WriteDownKey.F>

    fun onGetPhraseFailed(model: WriteDownKey.M): Next<WriteDownKey.M, WriteDownKey.F>

    fun onUserAuthenticated(model: WriteDownKey.M): Next<WriteDownKey.M, WriteDownKey.F>

    fun onPhraseRecovered(model: WriteDownKey.M, event: WriteDownKey.E.OnPhraseRecovered): Next<WriteDownKey.M, WriteDownKey.F>
}
