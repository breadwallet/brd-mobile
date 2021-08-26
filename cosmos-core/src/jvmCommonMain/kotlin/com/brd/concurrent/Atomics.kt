/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.concurrent

actual class AtomicReference<T> actual constructor(value: T) {

    private val ref = java.util.concurrent.atomic.AtomicReference(value)

    actual var value: T
        get() = ref.get()
        set(value) {
            ref.set(value)
        }
}
