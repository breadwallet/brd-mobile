/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2019 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@file:Suppress("NOTHING_TO_INLINE")

package com.brd.logger

/**
 * A simple and instantiable logger with a default implementation via [Companion].
 */
interface Logger {
    companion object : Logger {
        private val defaultLogger = DefaultLogger()

        fun create(tag: String): Logger = DefaultLogger(tag)

        override fun verbose(message: String, vararg data: Any?) =
            defaultLogger.verbose(message, *data)

        override fun debug(message: String, vararg data: Any?) =
            defaultLogger.debug(message, *data)

        override fun info(message: String, vararg data: Any?) =
            defaultLogger.info(message, *data)

        override fun warning(message: String, vararg data: Any?) =
            defaultLogger.warning(message, *data)

        override fun error(message: String, vararg data: Any?) =
            defaultLogger.error(message, *data)

        override fun wtf(message: String, vararg data: Any?) =
            defaultLogger.wtf(message, *data)
    }

    /** Log verbose [message] and any [data] objects. */
    fun verbose(message: String, vararg data: Any?)
    /** Log debug [message] and any [data] objects. */
    fun debug(message: String, vararg data: Any?)
    /** Log info [message] and any [data] objects. */
    fun info(message: String, vararg data: Any?)
    /** Log warning [message] and any [data] objects. */
    fun warning(message: String, vararg data: Any?)
    /** Log error [message] and any [data] objects. */
    fun error(message: String, vararg data: Any?)
    /** Log wtf [message] and any [data] objects. */
    fun wtf(message: String, vararg data: Any?)
}

/** Log verbose [message] and any [data] objects. */
inline fun logVerbose(message: String, vararg data: Any?) = Logger.verbose(message, *data)
/** Log debug [message] and any [data] objects. */
inline fun logDebug(message: String, vararg data: Any?) = Logger.debug(message, *data)
/** Log info [message] and any [data] objects. */
inline fun logInfo(message: String, vararg data: Any?) = Logger.info(message, *data)
/** Log warning [message] and any [data] objects. */
inline fun logWarning(message: String, vararg data: Any?) = Logger.warning(message, *data)
/** Log error [message] and any [data] objects. */
inline fun logError(message: String, vararg data: Any?) = Logger.error(message, *data)
/** Log wtf [message] and any [data] objects. */
inline fun logWtf(message: String, vararg data: Any?) = Logger.wtf(message, *data)

expect class DefaultLogger() : Logger {
    constructor(tag: String)
}
