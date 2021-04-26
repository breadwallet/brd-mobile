/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 breadwallet LLC
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
