/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/16/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.breadwallet.ui.send.AddressType

sealed class AddressResult {
    data class Success(val address: String, val destinationTag: String?) : AddressResult()
    object Invalid : AddressResult()
    object ExternalError : AddressResult()
    object NoAddress : AddressResult()
}

interface AddressResolverService {
    /** Resolves [target] to an [AddressResult] **/
    suspend fun resolveAddress(target: String, currencyCode: CurrencyCode, nativeCurrencyCode: CurrencyCode) : AddressResult
}

class AddressResolverServiceLocator (
    private val payIdService: PayIdService,
    private val fioService: FioService,
    private val unstoppableDomainService: UnstoppableDomainService
) {

    /** Returns the appropriate [AddressResolverService] for a given [addressType], null if none found **/
    fun getService(addressType: AddressType): AddressResolverService? = when (addressType) {
        is AddressType.Resolvable.PayId -> payIdService
        is AddressType.Resolvable.Fio -> fioService
        is AddressType.Resolvable.UnstoppableDomain -> unstoppableDomainService
        else -> null
    }
}