/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/5/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi

import com.autodesk.coroutineworker.CoroutineWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.addTimeInterval
import platform.Foundation.performBlock
import platform.Foundation.runUntilDate
import kotlin.coroutines.CoroutineContext

// Patch for https://github.com/Kotlin/kotlinx.coroutines/issues/770
actual fun runBlocking(block: suspend CoroutineScope.() -> Unit) {
    var completed = false

    GlobalScope.launch(MainRunLoopDispatcher) {
        CoroutineWorker.execute(block)
        completed = true
    }

    while (!completed) advanceRunLoop()
}

private object MainRunLoopDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        NSRunLoop.mainRunLoop().performBlock { block.run() }
    }
}

private fun advanceRunLoop() {
    NSRunLoop.mainRunLoop.runUntilDate(
        limitDate = NSDate().addTimeInterval(1.0) as NSDate
    )
}