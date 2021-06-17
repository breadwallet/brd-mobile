/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 4/1/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet

import com.breadwallet.ext.throttleLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

class FlowUtilsTest {

    // TODO: Use runBlockingTest: https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    @Ignore("Relies on a real clock, causing flakiness on slower environements.")
    fun throttleLatestTest() = runBlocking {
        assertEquals(
            listOf(1, 3),
            testFlow(count = 3, millis = 100)
                .throttleLatest(300)
                .toList()
        )

        assertEquals(
            listOf(1, 2, 3),
            testFlow(count = 3, millis = 100)
                .throttleLatest(100)
                .toList()
        )

        assertEquals(
            listOf(1, 10),
            testFlow(count = 10, millis = 100)
                .throttleLatest(1_000)
                .toList()
        )

        assertEquals(
            listOf(1, 3, 4, 5),
            flow {
                emit(1)
                delay(500)
                emit(2)
                delay(200)
                emit(3)
                delay(800)
                emit(4)
                delay(600)
                emit(5)
            }.throttleLatest(1_000).toList()
        )
    }

    private fun testFlow(count: Int, millis: Long) = flow {
        repeat(count) {
            emit(it + 1)
            delay(millis)
        }
    }
}
