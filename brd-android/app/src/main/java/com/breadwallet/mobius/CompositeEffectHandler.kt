/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> 7/22/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.mobius

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer

/**
 * Creates a [Connectable] that delegates connection creation to [effectHandlers]
 * and the corresponding [Connection]s.
 */
class CompositeEffectHandler<I, O> private constructor(
    private val effectHandlers: Array<out Connectable<I, O>>
) : Connectable<I, O> {

    companion object {
        fun <I, O> from(vararg effectHandlers: Connectable<I, O>) =
            CompositeEffectHandler(effectHandlers)
    }

    override fun connect(output: Consumer<O>): Connection<I> {
        val consumers = effectHandlers.map { it.connect(output) }

        return object : Connection<I> {
            override fun accept(value: I) =
                consumers.forEach { it.accept(value) }

            override fun dispose() =
                consumers.forEach { it.dispose() }
        }
    }
}
