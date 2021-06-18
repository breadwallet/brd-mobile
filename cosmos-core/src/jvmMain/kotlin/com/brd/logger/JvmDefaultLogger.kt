/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.logger

actual class DefaultLogger actual constructor() : Logger {

    private var tag: String? = null

    actual constructor(tag: String) : this() {
        this.tag = tag
    }

    private val tagString: String
        get() = if (tag == null) "" else "[$tag] "

    override fun verbose(message: String, vararg data: Any?) {
        println("${tagString}V/ $message")
    }

    override fun debug(message: String, vararg data: Any?) {
        println("${tagString}D/ $message")
    }

    override fun info(message: String, vararg data: Any?) {
        println("${tagString}I/ $message")
    }

    override fun warning(message: String, vararg data: Any?) {
        println("${tagString}W/ $message")
    }

    override fun error(message: String, vararg data: Any?) {
        println("${tagString}E/ $message")
    }

    override fun wtf(message: String, vararg data: Any?) {
        println("${tagString}WTF/ $message")
    }
}
