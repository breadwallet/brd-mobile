/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fastsync

import com.breadwallet.R
import com.breadwallet.model.SyncMode
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.util.CurrencyCode

object FastSync {

    data class M(
        val currencyCode: CurrencyCode,
        val fastSyncEnable: Boolean = false,
        val currencyId: String = ""
    ) {

        companion object {
            fun createDefault(currencyCode: CurrencyCode): M {
                return M(currencyCode)
            }
        }
    }

    sealed class E {
        object OnBackClicked : E()
        object OnLearnMoreClicked : E()
        object OnDisableFastSyncConfirmed : E()
        object OnDisableFastSyncCanceled : E()
        data class OnFastSyncChanged(val enable: Boolean) : E()
        data class OnSyncModesUpdated(val modeMap: Map<String, SyncMode>) : E()
        data class OnCurrencyIdsUpdated(val currencyMap: Map<String, String>) : E()
    }

    sealed class F {
        object LoadCurrencyIds : F()
        object ShowDisableFastSyncDialog : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AlertDialog(
                messageResId = R.string.WalletConnectionSettings_confirmation,
                positiveButtonResId = R.string.WalletConnectionSettings_turnOff,
                negativeButtonResId = R.string.Button_cancel
            )
        }

        sealed class Nav(
            override val navigationTarget: NavigationTarget
        ) : F(), NavigationEffect {
            object GoBack : Nav(NavigationTarget.Back)

            object GoToFaq : Nav(NavigationTarget.SupportPage(BRConstants.FAQ_FASTSYNC))
        }

        sealed class MetaData : F() {
            data class SetSyncMode(
                val currencyId: String,
                val mode: SyncMode
            ) : MetaData()

            object LoadSyncModes : MetaData()
        }
    }
}
