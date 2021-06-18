/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> {
    var lastEmissionMs = 0L
    return transform { value ->
        val currentMs = System.currentTimeMillis()
        if (currentMs - lastEmissionMs >= windowDuration) {
            lastEmissionMs = currentMs
            emit(value)
        }
    }
}

fun <T> Flow<T>.throttleLatest(
    windowDuration: Long
): Flow<T> = channelFlow {
    val mutex = Mutex()
    val hasLatest = AtomicBoolean(false)
    val latest = AtomicReference<T>(null)
    collectLatest { value ->
        if (mutex.tryLock()) {
            offer(value)
            launch(Dispatchers.Default) {
                delay(windowDuration)
                while (hasLatest.getAndSet(false) && isActive) {
                    offer(latest.getAndSet(null))
                    delay(windowDuration)
                }
                mutex.unlock()
            }
        } else {
            latest.set(value)
            hasLatest.set(true)
        }
    }
}
