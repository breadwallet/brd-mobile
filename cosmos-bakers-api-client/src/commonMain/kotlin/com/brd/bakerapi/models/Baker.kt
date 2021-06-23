/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Baker(
    val address: String,
    val name: String,
    val logo: String?,
    val balance: Double,
    val stakingBalance: Double,
    val stakingCapacity: Double,
    val maxStakingBalance: Double,
    val freeSpace: Double,
    val fee: Double,
    val minDelegation: Double,
    val payoutDelay: Double,
    val payoutPeriod: Double,
    val openForDelegation: Boolean,
    val estimatedRoi: Double,
    val serviceType: ServiceType,
    val serviceHealth: ServiceHealth,
    val payoutTiming: PayoutTiming,
    val payoutAccuracy: PayoutAccuracy,
    val audit: String? = null
)

@Serializable
enum class ServiceType {
    @SerialName("tezos_only")
    TEZOS_ONLY,
    @SerialName("multiasset")
    MULTIASSET,
    @SerialName("exchange")
    EXCHANGE,
    @SerialName("tezos_dune")
    TEZOS_DUNE;

    override fun toString() = name.toLowerCase()
}

@Serializable
enum class ServiceHealth{
    @SerialName("active")
    ACTIVE,
    @SerialName("closed")
    CLOSED,
    @SerialName("dead")
    DEAD;

    override fun toString() = name.toLowerCase()
}

@Serializable
enum class PayoutTiming {
    @SerialName("stable")
    STABLE,
    @SerialName("unstable")
    UNSTABLE,
    @SerialName("suspicious")
    SUSPICIOUS,
    @SerialName("no_data")
    NO_DATA;

    override fun toString() = name.toLowerCase()
}

@Serializable
enum class PayoutAccuracy {
    @SerialName("precise")
    PRECISE,
    @SerialName("inaccurate")
    INACCURATE,
    @SerialName("suspicious")
    SUSPICIOUS,
    @SerialName("no_data")
    NO_DATA;

    override fun toString() = name.toLowerCase()
}
