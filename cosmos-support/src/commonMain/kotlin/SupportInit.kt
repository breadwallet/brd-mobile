/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import kt.mobius.First
import kt.mobius.Init
import com.brd.support.SupportModel as M
import com.brd.support.SupportEffect as F


object SupportInit : Init<M, F> {

    override fun init(model: M): First<M, F> {
        return when (model.state) {
            is M.State.Initializing -> {
                First.first(
                    model,
                    setOfNotNull(
                        F.LoadArticles,
                        F.TrackEvent(model.event("appeared")),
                    )
                )
            }
            else -> First.first(model)
        }
    }
}
