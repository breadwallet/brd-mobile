/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/15/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver


import com.brd.logger.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val FIO_DELIMITER = "@"
private const val FIO_TESTNET_ENDPOINT = "https://testnet.fioprotocol.io/v1/chain/get_pub_address"
private const val FIO_MAINNET_ENDPOINT = "https://api.fio.services/v1/chain/get_pub_address"

private const val FIO_FIELD_PUBLIC_ADDRESS = "public_address"
private const val FIO_QUERY_KEY_DESTINATION_TAG = "dt"

internal fun String?.isFio() =
    this?.let {
        val parts = split(FIO_DELIMITER)
        parts.size == 2 && parts.all(String::isNotBlank)
    } ?: false

internal class FioService(
    private val httpClient: HttpClient,
    isMainnet: Boolean,
) : AddressResolverService {

    private val logger = Logger.create("FioService")

    private val endpoint = if (isMainnet) FIO_MAINNET_ENDPOINT else FIO_TESTNET_ENDPOINT

    override suspend fun resolveAddress(
        target: String,
        currencyCode: String,
        nativeCurrencyCode: String
    ): AddressResult {
        if (!target.isFio()) return AddressResult.Invalid

        val response = try {
            httpClient.post<JsonObject>(endpoint) {
                contentType(ContentType.Application.Json)
                body = buildJsonObject {
                    put("fio_address", target)
                    put("chain_code", nativeCurrencyCode)
                    put("token_code", currencyCode)
                }
            }
        } catch (e: Throwable) {
            logger.error("Failed to retrieve address.", e)
            return AddressResult.ExternalError
        }

        val addressString = response[FIO_FIELD_PUBLIC_ADDRESS]?.jsonPrimitive?.content
        return if (addressString == null) {
            logger.warning("No address results")
            AddressResult.NoAddress
        } else {
            val (address, queryString) = parseAddressString(addressString)
            val destinationTag = queryString[FIO_QUERY_KEY_DESTINATION_TAG]
            if (address.isBlank()) {
                logger.warning("No address results")
                AddressResult.NoAddress
            } else {
                AddressResult.Success(address, destinationTag)
            }
        }
    }

    private fun parseAddressString(addressStr: String): Pair<String, Map<String, String>> {
        val parts = addressStr.split("?")
        val address = parts[0]
        val queryStrMap = mutableMapOf<String, String>()

        try {
            if (parts.size > 1) {
                val kvs = parts[1].split("&")
                kvs.forEach {
                    val (key, value) = it.split("=")
                    queryStrMap[key] = value
                }
            }
        } catch (e: Exception) {
            logger.error("Malformed address string: $addressStr", e)
        }
        return Pair(address, queryStrMap)
    }
}
