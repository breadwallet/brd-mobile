/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/27/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.breadwallet.crypto.utility.CompletionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

suspend fun <R, E : Exception> asyncApiCall(
    body: CompletionHandler<R, E>.() -> Unit
): R = AsyncCompletionHandler<R, E>().apply(body).await()

private const val VALUE_RECEIVED_ERROR = "A value has already been received."

class AsyncCompletionHandler<R, E : Exception> : CompletionHandler<R, E> {

    private val result = MutableStateFlow<Pair<R?, E?>?>(null)

    override fun handleData(data: R) {
        check(result.value == null) { VALUE_RECEIVED_ERROR }
        result.value = data to null
    }

    override fun handleError(error: E) {
        check(result.value == null) { VALUE_RECEIVED_ERROR }
        result.value = null to error
    }

    suspend fun await(): R {
        val (data, error) = result.filterNotNull().first()
        return data ?: throw checkNotNull(error)
    }
}
