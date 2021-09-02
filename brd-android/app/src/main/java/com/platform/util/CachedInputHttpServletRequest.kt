/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/7/2019.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.util

import org.apache.commons.io.IOUtils
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/** Caches the result of [getInputStream] and returns the cached data on subsequent calls. */
class CachedInputHttpServletRequest(
    request: HttpServletRequest
) : HttpServletRequestWrapper(request) {

    private var cached = false
    private lateinit var cachedData: ByteArray

    override fun getInputStream(): ServletInputStream {
        if (!cached) {
            cachedData = IOUtils.toByteArray(super.getInputStream())
            cached = true
        }

        return object : ServletInputStream() {
            var finished = false
            val inputStream = cachedData.inputStream()

            override fun read(): Int =
                inputStream.read().also {
                    finished = it == -1
                }

            override fun isFinished() = finished
            override fun isReady() = true
            override fun setReadListener(readListener: ReadListener?) = Unit
        }
    }
}
