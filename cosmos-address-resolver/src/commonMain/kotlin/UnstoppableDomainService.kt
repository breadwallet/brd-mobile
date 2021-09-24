/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/16/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver

import com.brd.logger.Logger
import drewcarlson.blockset.BdbAddress
import drewcarlson.blockset.BdbAddresses
import drewcarlson.blockset.BdbAddressesError
import drewcarlson.blockset.BdbService


private const val SUFFIX_CNS = "crypto"
private const val SUFFIX_ENS = "eth"

internal fun String?.isCNS() =
    this?.let {
        val parts = split(".")
        parts.lastOrNull().equals(SUFFIX_CNS, true) && parts.all(String::isNotBlank)
    } ?: false

internal fun String?.isENS() =
    this?.let {
        val parts = split(".")
        parts.lastOrNull().equals(SUFFIX_ENS, true) && parts.all(String::isNotBlank)
    } ?: false

internal class UnstoppableDomainService(private val bdbService: BdbService) : AddressResolverService {
    private val logger = Logger.create("UnstoppableDomains")

    override suspend fun resolveAddress(
        target: String,
        currencyCode: String,
        nativeCurrencyCode: String
    ) : AddressResult {
        val currencyCodes = listOf(currencyCode.lowercase(), nativeCurrencyCode.lowercase()).distinct()
        return when (val result = bdbService.addressLookup(target, currencyCodes)) {
            is BdbAddresses -> {
                logger.debug("Target contains '${result.embedded.addresses.size}' results")
                val addressData = result.embedded.addresses.find { address ->
                    address.currencyCode.equals(currencyCode, true) ||
                            address.currencyCode.equals(nativeCurrencyCode, true)
                }
                logger.debug("Resolve status '${addressData?.status}'")
                when (addressData?.status) {
                    BdbAddress.Status.SUCCESS ->
                        AddressResult.Success(checkNotNull(addressData.address), null)
                    BdbAddress.Status.BLOCKCHAIN_IS_DOWN -> AddressResult.ExternalError
                    else -> AddressResult.Invalid
                }
            }
            is BdbAddressesError -> {
                logger.error("Address resolve request failed", result)
                AddressResult.ExternalError
            }
        }
    }
}
