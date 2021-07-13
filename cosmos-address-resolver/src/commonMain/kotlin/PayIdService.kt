/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 5/18/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver

import com.brd.logger.Logger
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

private const val PAY_ID_DELIMITER = "$"

private const val PAY_ID_VERSION = "1.0"
internal const val PAY_ID_ACCEPT_TYPE = "application/payid+json"

private const val PAY_ID_HEADER_VERSION = "PayID-Version"
private const val PAY_ID_FIELD_ADDRESSES = "addresses"
private const val PAY_ID_FIELD_ENVIRONMENT = "environment"
private const val PAY_ID_FIELD_CURRENCY = "paymentNetwork"
private const val PAY_ID_FIELD_ADDRESS_DETAILS = "addressDetails"
private const val PAY_ID_FIELD_ADDRESS = "address"
private const val PAY_ID_FIELD_TAG = "tag"

private const val PAY_ID_CURRENCY_ID_XRP = "XRPL"
private const val APP_CURRENCY_ID_XRP = "XRP"

internal fun String?.isPayId() =
    this?.let {
        val parts = split(PAY_ID_DELIMITER)
        parts.size == 2 && parts.all(String::isNotBlank)
    } ?: false

internal class PayIdService(private val isMainnet: Boolean) : AddressResolverService {
    // NOTE: payid requests will fail if the server sees the the 'application/json'
    // ContentType, even if the correct payid ContentType is provided.
    // This requires configuring a specific HttpClient JsonFeature instance that
    // only broadcasts the payid accept header
    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
            acceptContentTypes = listOf(ContentType.parse(PAY_ID_ACCEPT_TYPE))
            receive(object : ContentTypeMatcher {
                override fun contains(contentType: ContentType): Boolean = true
            })
        }
        defaultRequest {
            header(PAY_ID_HEADER_VERSION, PAY_ID_VERSION)
        }
    }
    private val logger = Logger.create("PayIdService")

    override suspend fun resolveAddress(
        target: String,
        currencyCode: String,
        nativeCurrencyCode: String
    ): AddressResult {
        if (!target.isPayId()) return AddressResult.Invalid
        val parts = target.split(PAY_ID_DELIMITER)
        val url = "https://${parts[1]}/${parts[0]}"

        val response = try {
            httpClient.get<JsonObject>(url)
        } catch (e: Throwable) {
            logger.error("Failed to retrieve address: $url.", e)
            return AddressResult.ExternalError
        }
        return try {
            val result = processResponse(response, nativeCurrencyCode) ?: Pair(null, null)
            result.let { (address, destinationTag) ->
                if (address.isNullOrBlank()) {
                    logger.error("No addresses found")
                    AddressResult.NoAddress
                } else {
                    AddressResult.Success(address, destinationTag)
                }
            }
        } catch (ex: Throwable) {
            logger.error("Failed to process response body", ex)
            AddressResult.ExternalError
        }
    }

    private fun processResponse(response: JsonObject, currencyCode: String): Pair<String, String?>? {
        val addressesArray = response[PAY_ID_FIELD_ADDRESSES]?.jsonArray
        if (addressesArray.isNullOrEmpty()) {
            logger.error("No addresses found.")
            return null
        }

        addressesArray.filterIsInstance<JsonObject>().forEach { addressObject ->
            val environment = addressObject[PAY_ID_FIELD_ENVIRONMENT]?.jsonPrimitive?.content ?: ""
            val payIdCurrency = addressObject[PAY_ID_FIELD_CURRENCY]?.jsonPrimitive?.content ?: ""
            val detailsObject = addressObject[PAY_ID_FIELD_ADDRESS_DETAILS]?.jsonObject
            if (isTargetEnvironment(environment)
                && isTargetCurrency(payIdCurrency, currencyCode)
                && detailsObject != null
            ) {
                val details = detailsObject.jsonObject
                return Pair(
                    checkNotNull(details[PAY_ID_FIELD_ADDRESS]?.jsonPrimitive?.content),
                    details[PAY_ID_FIELD_TAG]?.jsonPrimitive?.contentOrNull
                )
            }
        }

        return null
    }

    private fun isTargetEnvironment(environment: String) = when {
        environment.equals("TESTNET", true) -> !isMainnet
        else -> isMainnet
    }

    // Check if the currency ids match with special handling for XRP where
    // payid uses 'XRPL' and the app uses 'XRP'.
    private fun isTargetCurrency(payIdCurrency: String, currencyCode: String) =
        if (currencyCode.equals(APP_CURRENCY_ID_XRP, true)) {
            payIdCurrency.equals(PAY_ID_CURRENCY_ID_XRP, true)
        } else {
            payIdCurrency.equals(currencyCode, true)
        }
}
