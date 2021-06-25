/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/03/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.util

import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/** Default timeout in milliseconds to wait for a network request. */
private const val DEFAULT_TIMEOUT_MS = -1L

/** Default delay in milliseconds to wait before retrying a network request. */
private const val DEFAULT_RETRY_DELAY_MS = 2_000L

/** Default max delay in milliseconds before retrying a network request. */
private const val DEFAULT_MAX_DELAY_MS = 15_000L

/**
 * Calls the [block] suspend function and returns its value.
 *
 * If [block] throws a temporary error, it will be retried
 * up to [retryAttempts] times.
 *
 * If [block] does not return in less than [timeoutMs]
 * it will be cancelled and a retry attempt will occur if
 * not passed the retry limit.  The default is -1 meaning
 * no timeout will be used.
 *
 * Retry attempts are delayed by [delayMs] multiplied be the
 * current attempt index with a max of [maxDelayMs].
 */
suspend fun <R> netRetry(
    retryAttempts: Int,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    delayMs: Long = DEFAULT_RETRY_DELAY_MS,
    maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
    block: suspend () -> R
): R {
    repeat(retryAttempts - 1) { retryIndex ->
        logDebug("Retry attempt $retryIndex")

        try {
            return when (timeoutMs) {
                -1L -> block()
                else -> withTimeout(timeoutMs) { block() }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logWarning("Network call attempt failed", e)
        }
        val nextDelay = delayMs * (retryIndex + 1)
        delay(nextDelay.coerceAtMost(maxDelayMs))
    }
    return block()
}
