/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/7/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import drewcarlson.blockset.BdbAddress
import drewcarlson.blockset.BdbService


private const val SUFFIX_CNS = "crypto"
private const val SUFFIX_ENS = "eth"

fun String?.isCNS() =
    this?.let {
        val suffix = split(".").lastOrNull()
        suffix.equals(SUFFIX_CNS, true)
    } ?: false

fun String?.isENS() =
    this?.let {
        val suffix = split(".").lastOrNull()
        suffix.equals(SUFFIX_ENS, true)
    } ?: false

class UnstoppableDomainService(private val bdbService: BdbService) : AddressResolverService {
    override suspend fun resolveAddress(
        target: String,
        currencyCode: CurrencyCode,
        nativeCurrencyCode: CurrencyCode
    ) : AddressResult {
        val result = bdbService.addressLookup(target, listOf(currencyCode)).embedded.addresses.first()
        return when (result.status) {
            BdbAddress.Status.SUCCESS -> {
                if (result.address != null) AddressResult.Success(result.address!!, null)
                else AddressResult.NoAddress
            }
            BdbAddress.Status.BLOCKCHAIN_IS_DOWN -> AddressResult.ExternalError
            else -> AddressResult.Invalid
        }
    }
}