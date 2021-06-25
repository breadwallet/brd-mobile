/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/8/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.uigift

import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import dev.zacsweers.redacted.annotations.Redacted
import java.math.BigDecimal

object ShareGift {

    data class M(
        val txHash: String,
        @Redacted val shareUrl: String,
        @Redacted val recipientName: String,
        val pricePerUnit: BigDecimal,
        val giftAmount: BigDecimal,
        val giftAmountFiat: BigDecimal,
        val sharedImage: Boolean = false
    )

    sealed class E {
        object OnSendClicked : E()
    }

    sealed class F {
        data class ExportGiftImage(
            @Redacted val giftUrl: String,
            @Redacted val recipientName: String,
            val fiatPricePerUnit: BigDecimal,
            val giftAmount: BigDecimal,
            val giftAmountFiat: BigDecimal,
        ) : F(), ViewEffect
        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
    }
}
