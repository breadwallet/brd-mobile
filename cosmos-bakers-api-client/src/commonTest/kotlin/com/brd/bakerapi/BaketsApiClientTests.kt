/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi

import com.brd.bakerapi.models.BakerResult
import com.brd.bakerapi.BakersApiClient
import com.brd.bakerapi.models.BakersResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BakersApiClientTests {

    private lateinit var apiClient: BakersApiClient

    @BeforeTest
    fun before() {
        apiClient = BakersApiClient.create()
    }

    @Test
    fun testGetBakers() = runBlocking {
        val response = apiClient.getBakers()
        assertTrue(response is BakersResult.Success, "Error result: $response")
        assertTrue(response.bakers.isNotEmpty())
    }

    @Test
    fun testGetBaker() = runBlocking {
        val bakersResponse = apiClient.getBakers()
        assertTrue(bakersResponse is BakersResult.Success, "Error result: $bakersResponse")
        assertTrue(bakersResponse.bakers.isNotEmpty())

        val bakerAddress = bakersResponse.bakers.first().address
        val bakerResponse = apiClient.getBaker(bakerAddress)
        assertTrue(bakerResponse is BakerResult.Success, "Failure result: $bakerResponse")
    }
}