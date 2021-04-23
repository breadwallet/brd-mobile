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
package com.breadwallet.ui

import com.breadwallet.ui.send.SendSheet
import com.breadwallet.ui.send.SendSheetUpdate
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals

class SendSheetUpdateTests {

    @Test
    fun test_EnteringFiatAmount_RecalculatesCryptoAmount_AfterExchangeRateUpdate() {
        val model = SendSheet.M(
            currencyCode = "btc",
            fiatCode = "usd",
            fiatPricePerUnit = BigDecimal(100).setScale(2),
            balance = BigDecimal(1).setScale(8),
            amount = BigDecimal(".17176"),
            fiatAmount = BigDecimal("10050"),
            isAmountCrypto = false,
        )

        val event = SendSheet.E.OnExchangeRateUpdated(
            feeCurrencyCode = "btc",
            fiatPricePerFeeUnit = BigDecimal.ZERO,
            fiatPricePerUnit = BigDecimal("57859.2"),
        )

        val next = SendSheetUpdate.update(model, event)

        assertEquals(BigDecimal(".17369753"), next.modelUnsafe().amount)
    }
}
