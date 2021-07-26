/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.brd.concurrent.freeze

sealed class BrdApiHost {

    abstract val host: String

    object PRODUCTION : BrdApiHost() {
        override val host: String = "https://app.brd.com"
    }
    object STAGING : BrdApiHost() {
        override val host: String = "https://brd-web-staging.com"
    }
    object CANARY : BrdApiHost() {
        override val host: String = "https://canary.brd.com"
    }

    object LEGACY_PRODUCTION : BrdApiHost(){
        override val host: String = "https://api.breadwallet.com"
    }
    object LEGACY_STAGING : BrdApiHost() {
        override val host: String = "https://stage2.breadwallet.com"
    }

    class Custom(override val host: String) : BrdApiHost() {
        override fun hashCode(): Int = host.hashCode()
        override fun equals(other: Any?): Boolean = ((other as? Custom)?.host ?: other) == host
        override fun toString(): String = "Custom(host='$host')"
        init {
            freeze()
        }
    }

    operator fun component1(): String = host

    companion object {
        fun hostFor(isDebug: Boolean, isHydraActivated: Boolean): BrdApiHost {
            return if (isHydraActivated) {
                if (isDebug) STAGING else PRODUCTION
            } else {
                if (isDebug) LEGACY_STAGING else LEGACY_PRODUCTION
            }
        }
    }
}
