/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> 10/17/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.manager

import com.breadwallet.logger.Logger
import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logError
import com.breadwallet.logger.logInfo
import com.breadwallet.logger.logVerbose
import com.breadwallet.logger.logWarning
import com.breadwallet.logger.logWtf
import com.google.firebase.crashlytics.FirebaseCrashlytics

object BRReportsManager : Logger {
    @JvmStatic
    @JvmOverloads
    fun reportBug(er: Throwable?, crash: Boolean = false) {
        if (er == null) return
        logError("reportBug: ", er)
        try {
            FirebaseCrashlytics.getInstance().recordException(er)
        } catch (e: Exception) {
            logError("reportBug: failed to report to FireBase: ", e)
        }
        if (crash) throw er
    }

    override fun debug(message: String, vararg data: Any?) {
        logDebug(message, *data)
        log("D: $message", data)
    }

    override fun info(message: String, vararg data: Any?) {
        logInfo(message, *data)
        log("I: $message", data)
    }

    override fun verbose(message: String, vararg data: Any?) {
        logVerbose(message, *data)
        log("V: $message", data)
    }

    override fun warning(message: String, vararg data: Any?) {
        logWarning(message, *data)
        log("W: $message", data)
    }

    override fun wtf(message: String, vararg data: Any?) {
        logWtf(message, *data)
        log("WTF: $message", data)
    }

    override fun error(message: String, vararg data: Any?) {
        logError(message, *data)
        log("E: $message", data)
    }

    private fun log(message: String, data: Array<out Any?>) {
        FirebaseCrashlytics.getInstance().apply {
            log(message)
            data.filterIsInstance<Throwable>().forEach(::recordException)
        }
    }
}