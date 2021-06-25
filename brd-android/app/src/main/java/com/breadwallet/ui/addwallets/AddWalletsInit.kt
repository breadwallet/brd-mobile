/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.addwallets

import com.breadwallet.ui.addwallets.AddWallets.F
import com.breadwallet.ui.addwallets.AddWallets.M
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

val AddWalletsInit = Init<M, F> { model ->
    first(model, setOf<F>(F.SearchTokens(model.searchQuery)))
}
