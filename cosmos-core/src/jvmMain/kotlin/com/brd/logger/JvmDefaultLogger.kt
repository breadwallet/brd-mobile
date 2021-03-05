/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 breadwallet LLC
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
