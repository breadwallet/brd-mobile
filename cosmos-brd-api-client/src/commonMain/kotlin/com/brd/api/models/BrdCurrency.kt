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

@Serializable
data class BrdCurrency(
    val code: String,
    val name: String,
    val scale: Int,
    @SerialName("is_supported")
    val isSupported: Boolean,
    @SerialName("contract_address")
    val contractAddress: String,
    @SerialName("sale_address")
    val saleAddress: String,
    val colors: List<String>,
    val type: String,
    @SerialName("currency_id")
    val currencyId: String,
    @SerialName("alternate_names")
    val alternateNames: AltNames? = null,
    @SerialName("contract_info")
    val contractInfo: ContractInfo,
) {
    @Serializable
    data class AltNames(
        val coingecko: String? = null,
        val cryptocompare: String? = null,
    )

    @Serializable
    class ContractInfo(

    )
}
