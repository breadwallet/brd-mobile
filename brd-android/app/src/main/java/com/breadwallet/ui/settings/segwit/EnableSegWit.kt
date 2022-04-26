/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget

object EnableSegWit {

    data class M(val state: State = State.ENABLE) {
        enum class State {
            ENABLE, CONFIRMATION, DONE
        }
    }

    sealed class E {
        object OnEnableClick : E()
        object OnContinueClicked : E()
        object OnCancelClicked : E()
        object OnBackClicked : E()
        object OnDoneClicked : E()
    }

    sealed class F {
        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
        object GoToHome : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }
        object EnableSegWit : F()
    }
}
