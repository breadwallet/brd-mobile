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
        data.forEach { item ->
            println("${tagString}V/ $item")
        }
    }

    override fun debug(message: String, vararg data: Any?) {
        println("${tagString}D/ $message")
        data.forEach { item ->
            println("${tagString}D/ $item")
        }
    }

    override fun info(message: String, vararg data: Any?) {
        println("${tagString}I/ $message")
        data.forEach { item ->
            println("${tagString}I/ $item")
        }
    }

    override fun warning(message: String, vararg data: Any?) {
        println("${tagString}W/ $message")
        data.forEach { item ->
            println("${tagString}W/ $item")
        }
    }

    override fun error(message: String, vararg data: Any?) {
        println("${tagString}E/ $message")
        data.forEach { item ->
            println("${tagString}E/ $item")
        }
    }

    override fun wtf(message: String, vararg data: Any?) {
        println("${tagString}WTF/ $message")
        data.forEach { item ->
            println("${tagString}WTF/ $item")
        }
    }
}
