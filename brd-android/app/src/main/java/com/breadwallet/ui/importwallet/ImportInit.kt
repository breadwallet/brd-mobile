/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.importwallet

import com.breadwallet.ui.importwallet.Import.F
import com.breadwallet.ui.importwallet.Import.M
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

val ImportInit = Init<M, F> { model ->
    if (model.privateKey != null && !model.isKeyValid) {
        first(
            model,
            setOf(
                F.ValidateKey(model.privateKey, model.keyPassword)
            )
        )
    } else first(model)
}
