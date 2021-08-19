/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.writedownkey

import android.security.keystore.UserNotAuthenticatedException
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.writedownkey.WriteDownKey.E
import com.breadwallet.ui.writedownkey.WriteDownKey.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createWriteDownKeyHandler(
    userManager: BrdUserManager
) = subtypeEffectHandler<F, E> {
    addFunction<F.GetPhrase> {
        val rawPhrase = try {
            userManager.getPhrase()
        } catch (e: UserNotAuthenticatedException) {
            null
        }
        if (rawPhrase == null || rawPhrase.isEmpty()) {
            E.OnGetPhraseFailed
        } else {
            val phrase = String(rawPhrase).split(" ")
            E.OnPhraseRecovered(phrase)
        }
    }
    addConsumer<F.TrackEvent> { (event) ->
        EventUtils.pushEvent(event)
    }
}
