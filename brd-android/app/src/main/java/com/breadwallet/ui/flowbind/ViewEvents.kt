/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.flowbind

import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

fun View.clicks(): Flow<Unit> =
    callbackFlow {
        setOnClickListener { offer(Unit) }
        awaitClose { setOnClickListener(null) }
    }.flowOn(Dispatchers.Main)

fun View.longClicks(): Flow<Unit> =
    callbackFlow {
        setOnLongClickListener { offer(Unit) }
        awaitClose { setOnLongClickListener(null) }
    }.flowOn(Dispatchers.Main)

fun View.touchEvents(consumed: Boolean = true): Flow<MotionEvent> =
    callbackFlow {
        setOnTouchListener { _, event ->
            offer(event)
            consumed
        }
        awaitClose { setOnTouchListener(null) }
    }.flowOn(Dispatchers.Main)

