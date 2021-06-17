/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget

object FingerprintSettings {

    data class M(
        val unlockApp: Boolean = false,
        val sendMoney: Boolean = false,
        val sendMoneyEnable: Boolean = false
    )

    sealed class E {
        object OnBackClicked : E()
        object OnFaqClicked : E()
        data class OnAppUnlockChanged(val enable: Boolean) : E()
        data class OnSendMoneyChanged(val enable: Boolean) : E()
        data class OnSettingsLoaded(
            val unlockApp: Boolean,
            val sendMoney: Boolean
        ) : E()
    }

    sealed class F {
        object LoadCurrentSettings : F()
        data class UpdateFingerprintSetting(
            val unlockApp: Boolean,
            val sendMoney: Boolean
        ) : F()

        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
        object GoToFaq : F(), NavigationEffect {
            override val navigationTarget =
                NavigationTarget.SupportPage(BRConstants.FAQ_ENABLE_FINGERPRINT)
        }
    }
}
