/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import com.bluelinelabs.conductor.Router
import com.brd.exchange.ExchangeEffect
import com.breadwallet.tools.util.EventUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kt.mobius.Connection

class AndroidExchangeEffectHandler(
    private val router: Router,
) : Connection<ExchangeEffect> {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun accept(value: ExchangeEffect) {
        when (value) {
            is ExchangeEffect.TrackEvent -> EventUtils.pushEvent(value.name, value.props)
            ExchangeEffect.ExitFlow -> scope.launch(Main) {
                if (router.backstack.lastOrNull()?.controller is ExchangeController) {
                    router.popCurrentController()
                }
            }
            else -> Unit
        }
    }

    override fun dispose() = Unit
}
