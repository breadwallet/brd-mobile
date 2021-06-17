/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.txdetails

import com.breadwallet.ui.txdetails.TxDetails.F
import com.breadwallet.ui.txdetails.TxDetails.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object TxDetailsInit : Init<M, F> {
    override fun init(model: M): First<M, F> {
        return First.first(
            model, setOf(
                F.LoadTransaction(model.currencyCode, model.transactionHash),
                F.LoadTransactionMetaData(model.currencyCode, model.transactionHash)
            )
        )
    }
}
