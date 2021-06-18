/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/27/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.breadwallet.tools.crypto.CryptoHelper.hexEncode
import com.breadwallet.tools.crypto.CryptoHelper.keccak256
import com.breadwallet.tools.util.EventUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val ETH_ADDRESS_BYTES = 20

fun ByteArray?.pubKeyToEthAddress(): String? = when {
    this == null || isEmpty() -> null
    else -> {
        val addressBytes = keccak256(sliceArray(1..lastIndex))
            ?.takeLast(ETH_ADDRESS_BYTES)
            ?.toByteArray()
        if (addressBytes?.size == ETH_ADDRESS_BYTES) {
            "0x${hexEncode(addressBytes)}"
        } else null
    }
}

private fun sendMismatchEvent(
    ethAddressHash: String,
    rewardsIdHash: String,
    ethBalance: String,
    tokenBalances: List<Pair<String, String>>
) {
    val tokens = tokenBalances.map { (currencyCode, balance) ->
        "has_balance_$currencyCode" to balanceString(balance)
    }
    EventUtils.pushEvent(
        EventUtils.EVENT_PUB_KEY_MISMATCH,
        mapOf(
            EventUtils.EVENT_ATTRIBUTE_REWARDS_ID_HASH to rewardsIdHash,
            EventUtils.EVENT_ATTRIBUTE_ADDRESS_HASH to ethAddressHash,
            "has_balance_eth" to balanceString(ethBalance)
        ) + tokens
    )

    FirebaseCrashlytics.getInstance().apply {
        log("rewards_id_hash = $rewardsIdHash")
        log("old_address_hash = $ethAddressHash")
        log("has_balance_eth = ${balanceString(ethBalance)}")
        tokens.forEach { (key, balance) ->
            log("$key = $balance")
        }
        recordException(IllegalStateException("eth address mismatch"))
    }
}

private fun balanceString(string: String) = when (string) {
    "unknown" -> "unknown"
    "0x0", "0" -> "no"
    else -> "yes"
}
