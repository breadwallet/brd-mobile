/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.sync

import com.breadwallet.R
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.util.CurrencyCode

object SyncBlockchain {
    data class M(val currencyCode: CurrencyCode)

    sealed class E {
        object OnFaqClicked : E()
        object OnSyncClicked : E()
        object OnConfirmSyncClicked : E()

        object OnSyncStarted : E()
    }

    sealed class F {
        data class SyncBlockchain(
            val currencyCode: CurrencyCode
        ) : F()


        sealed class Nav(
            override val navigationTarget: NavigationTarget
        ) : F(), NavigationEffect {

            object ShowSyncConfirmation : Nav(
                NavigationTarget.AlertDialog(
                    messageResId = R.string.ReScan_footer,
                    titleResId = R.string.ReScan_alertTitle,
                    positiveButtonResId = R.string.ReScan_alertAction,
                    negativeButtonResId = R.string.Button_cancel
                )
            )

            object GoToHome : Nav(NavigationTarget.Home)

            data class GoToSyncFaq(
                val currencyCode: CurrencyCode
            ) : Nav(NavigationTarget.SupportPage(BRConstants.FAQ_RESCAN))
        }
    }
}
