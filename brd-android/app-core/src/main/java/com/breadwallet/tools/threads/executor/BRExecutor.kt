/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> 10/25/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.threads.executor

import com.breadwallet.logger.logError
import com.breadwallet.tools.manager.BRReportsManager
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

/*
 * Singleton class for default executor supplier
 */
object BRExecutor : RejectedExecutionHandler {
    private val backgroundExecutor = Default.asExecutor()
    private val mainThreadExecutor = Main.asExecutor()
    /**
     * Returns the thread pool executor for light weight background task such as interacting with the Core or BRKeyStore.
     */
    fun forLightWeightBackgroundTasks(): Executor {
        return backgroundExecutor
    }

    /**
     * Returns the thread pool executor for main thread task.
     */
    fun forMainThreadTasks(): Executor {
        return mainThreadExecutor
    }

    override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
        logError("rejectedExecution: ")
        BRReportsManager.reportBug(RejectedExecutionException("rejectedExecution: core pool size: ${executor.corePoolSize}"))
    }

    @JvmStatic
    fun getInstance() = this
}