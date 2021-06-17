/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/8/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.uigift

import com.breadwallet.ui.uigift.ShareGift.M
import com.breadwallet.ui.uigift.ShareGift.E
import com.breadwallet.ui.uigift.ShareGift.F
import com.spotify.mobius.Next
import com.spotify.mobius.Update

object ShareGiftUpdate : Update<M, E, F> {
    override fun update(model: M, event: E): Next<M, F> {
        return when (event) {
            E.OnSendClicked -> onSendClicked(model)
        }
    }

    private fun onSendClicked(model: M): Next<M, F> =
        if (model.sharedImage) {
            Next.dispatch(setOf(F.GoBack))
        } else {
            Next.next(
                model.copy(sharedImage = false),
                setOf(
                    F.ExportGiftImage(
                        model.shareUrl,
                        model.recipientName,
                        model.pricePerUnit,
                        model.giftAmount,
                        model.giftAmountFiat
                    )
                )
            )
        }
}
