/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.logger

import platform.Foundation.NSLog

actual class DefaultLogger actual constructor() : Logger {

    private var tag: String? = null

    actual constructor(tag: String) : this() {
        this.tag = tag
    }

    private val tagString: String
        get() = if (tag == null) "" else "[$tag] "

    override fun verbose(message: String, vararg data: Any?) {
        NSLog("${tagString}V/ $message")
        data.forEach { item ->
            NSLog("${tagString}V/ $item")
        }
    }

    override fun debug(message: String, vararg data: Any?) {
        NSLog("${tagString}D/ $message")
        data.forEach { item ->
            NSLog("${tagString}D/ $item")
        }
    }

    override fun info(message: String, vararg data: Any?) {
        NSLog("${tagString}I/ $message")
        data.forEach { item ->
            NSLog("${tagString}I/ $item")
        }
    }

    override fun warning(message: String, vararg data: Any?) {
        NSLog("${tagString}W/ $message")
        data.forEach { item ->
            NSLog("${tagString}W/ $item")
        }
    }

    override fun error(message: String, vararg data: Any?) {
        NSLog("${tagString}E/ $message")
        data.forEach { item ->
            NSLog("${tagString}E/ $item")
        }
    }

    override fun wtf(message: String, vararg data: Any?) {
        NSLog("${tagString}WTF/ $message")
        data.forEach { item ->
            NSLog("${tagString}WTF/ $item")
        }
    }
}
