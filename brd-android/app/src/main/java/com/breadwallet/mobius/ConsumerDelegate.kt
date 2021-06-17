/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.mobius

import com.spotify.mobius.functions.Consumer

class ConsumerDelegate<V>(
    initial: Consumer<V>? = null
) : Consumer<V> {

    var consumer: Consumer<V>? = initial
        @Synchronized get
        @Synchronized set

    override fun accept(value: V) {
        consumer?.accept(value)
    }
}