/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import kt.mobius.First
import kt.mobius.Init
import com.brd.exchange.ExchangeEffect as F
import com.brd.exchange.ExchangeModel as M

object ExchangeInit : Init<M, F> {

    override fun init(model: M): First<M, F> {
        return when (model.state) {
            is M.State.Initializing -> {
                First.first(
                    model,
                    setOfNotNull(
                        F.LoadCountries,
                        F.TrackEvent(model.event("appeared")),
                    )
                )
            }
            is M.State.OrderSetup -> {
                if (model.offerState == M.OfferState.COMPLETED) {
                    First.first(model)
                } else {
                    First.first(model, F.RequestOffers(model.offerBodyOrNull(), model.mode))
                }
            }
            else -> First.first(model)
        }
    }
}
