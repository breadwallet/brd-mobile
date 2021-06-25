/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.provekey

import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.provekey.PaperKeyProve.E
import com.breadwallet.ui.provekey.PaperKeyProve.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createPaperKeyProveHandler() = subtypeEffectHandler<F, E> {
    addFunction<F.StoreWroteDownPhrase> {
        BRSharedPrefs.phraseWroteDown = true
        E.OnWroteDownKeySaved
    }
    addConsumer<F.TrackEvent> { (event) ->
        EventUtils.pushEvent(event)
    }
}
