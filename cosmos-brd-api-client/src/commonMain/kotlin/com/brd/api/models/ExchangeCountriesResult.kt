/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class ExchangeCountriesResult {

    @Serializable
    data class Success(
        @SerialName("detected_country_code")
        val detectedCountryCode: String,
        @SerialName("detected_region_code")
        val detectedRegionCode: String,
        val countries: List<ExchangeCountry>
    ) : ExchangeCountriesResult()

    data class Error(
        override val status: Int,
        override val body: String,
    ) : ExchangeCountriesResult(), ApiResponseError
}
