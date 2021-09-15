/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import com.brd.util.Formatters.fiat
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val AMOUNT = 1000.0

class FormatterTests {

    @Test
    fun testCurrencyToLocaleMap() {
        currencyToLocaleMap.forEach { (_, localeId) ->
            assertTrue(hasLocale(localeId), "Expected locale for '$localeId'")
        }
    }

    @Test
    fun testJpyFormat() {
        assertContains(
            listOf(
                "¥1,000", // iOS
                "￥1,000", // full-width, jvm
            ),
            fiat("jpy").format(AMOUNT)
        )
    }

    @Test
    fun testUsdFormat() {
        assertEquals("$1,000.00", fiat("usd").format(AMOUNT))
    }

    @Test
    fun testCadFormat() {
        assertEquals("$1,000.00", fiat("cad").format(AMOUNT))
    }

    @Test
    fun testEurFormat() {
        assertContains(
            listOf(
                "1.000,00 €", // NBSP, iOS
                "1.000,00 €", // jvm
            ),
            fiat("eur").format(AMOUNT)
        )
    }

    @Test
    fun testCnyFormat() {
        assertContains(
            listOf(
                "¥1,000.00", // iOS
                "￥1,000.00", // full-width, jvm
            ),
            fiat("cny").format(AMOUNT)
        )
    }

    @Test
    fun testVndFormat() {
        assertContains(
            listOf(
                "1.000 ₫", // NBSP - formal symbol, iOS
                "1.000 đ", // informal symbol, jvm
            ),
            fiat("vnd").format(AMOUNT)
        )
    }

    @Test
    fun testGbpFormat() {
        assertEquals("£1,000.00", fiat("gbp").format(AMOUNT))
    }

    @Test
    fun testInrFormat() {
        assertContains(
            listOf(
                "₹1,000.00", // iOS, android
                "रू १,०००.००", // desktop jvm
            ),
            fiat("inr").format(AMOUNT)
        )
    }

    @Test
    fun testBrlFormat() {
        assertContains(
            listOf(
                "R$ 1.000,00", // NBSP, iOS
                "R$ 1.000,00", // jvm
            ),
            fiat("brl").format(AMOUNT)
        )
    }

    @Test
    fun testPlnFormat() {
        assertContains(
            listOf(
                "1000,00 zł", // NBSP, iOS
                "1 000 zł", // NBSP, jvm
                "1 000,00 zł", // jvm
            ),
            fiat("pln").format(AMOUNT)
        )
    }

    @Test
    fun testFormatWithNumber() {
        val subject = Formatters.crypto("123")
        assertEquals("1 123", subject.format(1.0))
    }
}

expect fun hasLocale(id: String): Boolean
