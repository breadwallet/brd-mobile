/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
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
