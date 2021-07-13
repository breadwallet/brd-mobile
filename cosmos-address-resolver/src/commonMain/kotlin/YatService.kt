/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 7/8/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.addressresolver

import com.brd.logger.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.native.concurrent.SharedImmutable

private const val YAT_API_URL = "https://a.y.at/emoji_id/"
private const val KEY_TAG = "tag"
private const val KEY_DATA = "data"
private const val KEY_RESULT = "result"
private const val BTC_TAG = 0x1003
private const val ETH_TAG = 0x1004

@SharedImmutable
private val CRYPTO_TAG_RANGE = 0x1004..0x3fff

internal fun String?.isYatId(): Boolean =
    this?.toCharArray()?.any(Char::isSurrogate) ?: false

internal class YatService(private val httpClient: HttpClient) : AddressResolverService {

    private val logger = Logger.create("YatService")

    override suspend fun resolveAddress(
        target: String,
        currencyCode: String,
        nativeCurrencyCode: String
    ): AddressResult {
        val response = try {
            httpClient.get<JsonObject>(YAT_API_URL + target)
        } catch (e: Throwable) {
            logger.error("Failed to resolve address", e)
            return AddressResult.ExternalError
        }

        val records = response[KEY_RESULT]
            ?.jsonArray
            ?.filterIsInstance<JsonObject>()
            ?: return AddressResult.NoAddress
        val record = when {
            nativeCurrencyCode.equals("eth", true) -> {
                records.find { record ->
                    record[KEY_TAG]?.jsonPrimitive?.int == ETH_TAG
                }
            }
            nativeCurrencyCode.equals("btc", true) -> {
                records.find { record ->
                    record[KEY_TAG]?.jsonPrimitive?.int == BTC_TAG
                }
            }
            else -> {
                records.find { record ->
                    record[KEY_TAG]?.jsonPrimitive?.int in CRYPTO_TAG_RANGE &&
                            record[KEY_DATA]?.jsonPrimitive?.content?.run {
                                startsWith(currencyCode, true) ||
                                        startsWith(nativeCurrencyCode, true)
                            } ?: false
                }
            }
        }

        return if (record == null) {
            AddressResult.NoAddress
        } else {
            val data = checkNotNull(record[KEY_DATA]).jsonPrimitive.content
            AddressResult.Success(data.substringAfter(":"), null)
        }
    }
}
