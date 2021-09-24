/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 7/13/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddressTypeTests {

    @Test
    fun testPayId() {
        assertTrue("GiveDirectly\$payid.charity".isPayId())
        assertTrue("test5\$payid.test.coinselect.com".isPayId())
        assertTrue("reza\$payid.test.coinselect.com".isPayId())
        assertTrue("pay\$wietse.com".isPayId())
        assertTrue("john.smith\$dev.payid.es".isPayId())
        assertTrue("pay\$zochow.ski".isPayId())

        assertFalse("".isPayId())
        assertFalse("test5payid.test.coinselect.com".isPayId())
        assertFalse("payid.test.coinselect.com".isPayId())
        assertFalse("rAPERVgXZavGgiGv6xBgtiZurirW2yAmY".isPayId())
        assertFalse("unknown".isPayId())
        assertFalse("0x2c4d5626b6559927350db12e50143e2e8b1b9951".isPayId())
        assertFalse("\$payid.charity".isPayId())
        assertFalse("payid.charity$".isPayId())
    }

    @Test
    fun testFio() {
        assertTrue("luke@stokes".isFio())

        assertFalse("".isFio())
        assertFalse("invalid".isFio())
        assertFalse("luke@".isFio())
        assertFalse("@stokes".isFio())
    }

    @Test
    fun testUnstoppableDomains() {
        assertTrue("vitalik.eth".isENS())
        assertTrue("brad.crypto".isCNS())

        assertFalse("".isENS())
        assertFalse("unknown".isENS())
        assertFalse("vitalik.com".isENS())
        assertFalse(".eth".isENS())
        assertFalse("vitalik.".isENS())

        assertFalse("".isCNS())
        assertFalse("unknown".isCNS())
        assertFalse("brad.com".isCNS())
        assertFalse(".crypto".isCNS())
        assertFalse("brad.".isCNS())
    }
}
