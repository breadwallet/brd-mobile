/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/22/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet

import com.breadwallet.ui.pin.InputPin
import org.junit.Test
import kotlin.test.assertTrue

class RedactedTest {

    @Test
    fun testRedaction() {
        val event = InputPin.E.OnPinEntered(pin = "0000", isPinCorrect = true)
        assertTrue(event.toString().contains("pin=***"))
    }
}
