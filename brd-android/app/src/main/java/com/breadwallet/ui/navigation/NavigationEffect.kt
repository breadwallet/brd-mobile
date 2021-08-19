/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 8/1/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.navigation

import com.breadwallet.ui.ViewEffect

/**
 * [NavigationEffect] can be applied to a screen specific
 * navigation effect to support [RouterNavigator]
 * without needing to map every effect to a [NavigationTarget].
 */
interface NavigationEffect : ViewEffect {
    val navigationTarget: INavigationTarget
}
