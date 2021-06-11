/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/7/21.
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
package com.breadwallet.util

import com.breadwallet.logger.logError
import drewcarlson.blockset.BdbAddress
import drewcarlson.blockset.BdbAddresses
import drewcarlson.blockset.BdbAddressesError
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
        return when (val response = bdbService.addressLookup(target, currencyCode)) {
            is BdbAddresses -> {
                val result = response.embedded.addresses.first()
                when (result.status) {
                    BdbAddress.Status.SUCCESS -> {
                        result.address?.let { address ->
                            AddressResult.Success(address, null)
                        } ?: AddressResult.NoAddress
                    }
                    BdbAddress.Status.BLOCKCHAIN_IS_DOWN -> AddressResult.ExternalError
                    else -> AddressResult.Invalid
                }
            }
            is BdbAddressesError -> {
                logError("Address lookup failed: $response")
                AddressResult.ExternalError
            }
        }
    }
}
