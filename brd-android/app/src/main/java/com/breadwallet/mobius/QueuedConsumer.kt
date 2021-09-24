/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.mobius

import com.spotify.mobius.functions.Consumer
import kt.mobius.functions.Consumer as KtConsumer


/**
 * Collects events and passes them in order to a new consumer via [dequeueAll].
 */
class QueuedConsumer<V> : Consumer<V> {

    private val queue = arrayListOf<V>()

    override fun accept(value: V) = synchronized<Unit>(queue) {
        queue.add(value)
    }

    fun dequeueAll(target: Consumer<V>) = synchronized(queue) {
        queue.forEach(target::accept)
        queue.clear()
    }
}

class QueuedConsumerKt<V> : KtConsumer<V> {

    private val queue = arrayListOf<V>()

    override fun accept(value: V) = synchronized<Unit>(queue) {
        queue.add(value)
    }

    fun dequeueAll(target: KtConsumer<V>) = synchronized(queue) {
        queue.forEach(target::accept)
        queue.clear()
    }
}