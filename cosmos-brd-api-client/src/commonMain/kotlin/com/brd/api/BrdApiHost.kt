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
package com.brd.api

sealed class BrdApiHost(open val host: String) {
    object PRODUCTION : BrdApiHost("https://app.brd.com")
    object STAGING : BrdApiHost("https://brd-web-staging.com")
    object CANARY : BrdApiHost("https://canary.brd.com")

    object LEGACY_PRODUCTION : BrdApiHost("https://api.breadwallet.com")
    object LEGACY_STAGING : BrdApiHost("https://stage2.breadwallet.com")

    class Custom(host: String) : BrdApiHost(host) {
        override fun hashCode(): Int = host.hashCode()
        override fun equals(other: Any?): Boolean = ((other as? Custom)?.host ?: other) == host
        override fun toString(): String = "Custom(host='$host')"
    }

    operator fun component1(): String = host
}
