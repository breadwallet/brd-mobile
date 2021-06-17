/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

package com.brd.bakerapi

import com.brd.bakerapi.models.Baker
import com.brd.bakerapi.models.BakerResult
import com.brd.bakerapi.models.BakersResult
import io.ktor.client.*

interface BakersApiClient {

    companion object {
        fun create(
            httpClient: HttpClient = HttpClient()
        ): BakersApiClient = BakingBadApiClient(httpClient = httpClient)
    }

    /**
     * Fetch a list of [Baker]s.
     */
    suspend fun getBakers() : BakersResult

    /**
     * Fetch the [Baker] for the given [address]
     */
    suspend fun getBaker(address: String) : BakerResult
}