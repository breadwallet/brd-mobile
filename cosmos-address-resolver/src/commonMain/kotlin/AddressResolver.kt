/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/16/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver

import com.brd.concurrent.freeze
import com.brd.logger.Logger
import drewcarlson.blockset.BdbService
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable


sealed class AddressType {
    object NativePublic : AddressType()

    sealed class Resolvable : AddressType() {
        object PayId : Resolvable()
        object Fio : Resolvable()
        //object Yat : Resolvable()

        sealed class UnstoppableDomain : Resolvable() {
            object ENS : UnstoppableDomain()
            object CNS : UnstoppableDomain()
        }
    }
}

sealed class AddressResult {
    data class Success(val address: String, val destinationTag: String?) : AddressResult()
    object Invalid : AddressResult()
    object ExternalError : AddressResult()
    object NoAddress : AddressResult()
}

internal interface AddressResolverService {
    /** Resolves [target] to an [AddressResult] **/
    suspend fun resolveAddress(target: String, currencyCode: String, nativeCurrencyCode: String): AddressResult
}

@SharedImmutable
internal val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

class AddressResolver(
    bdbService: BdbService,
    isMainnet: Boolean,
) {
    private val http = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    private val logger = Logger.create("AddressResolver")
    //private val yatService = YatService(http)
    private val payIdService = PayIdService(isMainnet)
    private val fioService = FioService(http, isMainnet)
    private val unstoppableDomainService = UnstoppableDomainService(bdbService)

    init {
        freeze()
    }

    /** Returns the appropriate [AddressResolverService] for a given [addressType], null if none found **/
    internal fun getService(addressType: AddressType): AddressResolverService? = when (addressType) {
        AddressType.Resolvable.PayId -> payIdService
        AddressType.Resolvable.Fio -> fioService
        AddressType.Resolvable.UnstoppableDomain.CNS,
        AddressType.Resolvable.UnstoppableDomain.ENS -> unstoppableDomainService
        //AddressType.Resolvable.Yat -> yatService
        AddressType.NativePublic -> null
    }

    fun getAddressType(address: String): AddressType? {
        return when {
            address.isBlank() -> null
            address.isCNS() -> AddressType.Resolvable.UnstoppableDomain.CNS
            address.isENS() -> AddressType.Resolvable.UnstoppableDomain.ENS
            address.isPayId() -> AddressType.Resolvable.PayId
            address.isFio() -> AddressType.Resolvable.Fio
            //address.isYatId() -> AddressType.Resolvable.Yat
            else -> AddressType.NativePublic
        }
    }

    suspend fun resolveAddress(target: String, currencyCode: String, nativeCurrencyCode: String): AddressResult {
        val addressType = getAddressType(target)
        if (addressType == null) {
            logger.warning("Invalid resolvable target '$target'")
            return AddressResult.Invalid
        }
        return resolveAddress(addressType, target, currencyCode, nativeCurrencyCode)
    }

    suspend fun resolveAddress(
        addressType: AddressType,
        target: String,
        currencyCode: String,
        nativeCurrencyCode: String
    ): AddressResult {
        if (addressType is AddressType.NativePublic) {
            logger.warning("Invalid resolvable target '$target'")
            return AddressResult.Invalid
        }
        val service = getService(addressType)
        if (service == null) {
            logger.warning("Service not found for '$addressType'")
            return AddressResult.Invalid
        }
        return Default { service.resolveAddress(target, currencyCode, nativeCurrencyCode) }
    }
}
