/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/31/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.breadwallet.logger.logError
import com.breadwallet.tools.manager.BRReportsManager
import kotlinx.coroutines.CoroutineExceptionHandler

fun Any.errorHandler(
    tag: String = ""
) = CoroutineExceptionHandler { _, e ->
    val message = "Exception in coroutine: '${this::class.simpleName}#$tag'"
    logError(message, e)
    BRReportsManager.reportBug(Exception(message, e))
}
