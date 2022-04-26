/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.breadwallet.logger.logDebug
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

/** Immediately closes BreadBox.  */
class BreadBoxCloseWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams),
    DIAware {

    override val di by closestDI { context.applicationContext }

    override fun doWork() =
        with(direct.instance<BreadBox>()) {
            if (isOpen) {
                logDebug("Closing BreadBox")
                close()
            } else {
                logDebug("BreadBox already closed")
            }
            Result.success()
        }

    companion object {
        private const val TAG_CLOSE_BREADBOX = "background-close-breadbox"
        private const val CLOSE_DELAY_SECONDS = 30

        /** Enqueue job with a delay of [CLOSE_DELAY_SECONDS]. */
        fun enqueueWork() {
            logDebug("Enqueueing BreadBoxCloseWorker")
            WorkManager.getInstance().enqueue(
                OneTimeWorkRequestBuilder<BreadBoxCloseWorker>()
                    .setInitialDelay(CLOSE_DELAY_SECONDS.toLong(), TimeUnit.SECONDS)
                    .addTag(TAG_CLOSE_BREADBOX)
                    .build()
            )
        }

        /** Cancel enqueued [BreadBoxCloseWorker]. */
        fun cancelEnqueuedWork() {
            logDebug("Cancelling BreadBoxCloseWorker")
            WorkManager.getInstance().cancelAllWorkByTag(TAG_CLOSE_BREADBOX)
        }
    }
}
