/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.*

/**
 * A [JsonTransformingSerializer] capable of interpreting packed
 * JSON arrays as objects by extracting elements into ordered
 * object properties.
 *
 * Serialized:
 * [
 *   [
 *     "ant",
 *     "bat",
 *     "8.62470862470862470862",
 *     "2021-02-24T05:45:13.975170Z"
 *   ],
 *   ...
 * ]
 *
 * Deserialized:
 * data class ExchangePair(
 *   fromCode = "ant",
 *   toCode = "bat",
 *   rate = 8.62470862470862470862,
 *   timestamp = "2021-02-24T05:45:13.975170Z"
 * )
 */
@OptIn(ExperimentalSerializationApi::class)
internal open class ArrayPackedObjectTransformer<T>(
    serializer: KSerializer<T>
) : JsonTransformingSerializer<List<T>>(
    ListSerializer(serializer)
) {

    override fun transformSerialize(element: JsonElement): JsonElement {
        check(element is JsonArray) { "Expected element '$element' to be a JsonArray." }
        val elementNames = descriptor.elementDescriptors.single().elementNames
        return element.map { child ->
            check(child is JsonObject) { "Expected element '$child' to be a JsonArray." }
            buildJsonArray {
                elementNames.forEach { name ->
                    add(checkNotNull(child[name]))
                }
            }
        }.let(::JsonArray)
    }

    override fun transformDeserialize(element: JsonElement): JsonElement {
        check(element is JsonArray) { "Expected element '$element' to be a JsonArray." }
        val elementNames = descriptor.elementDescriptors.single().elementNames
        return element.map { child ->
            check(child is JsonArray) { "Expected element '$child' to be a JsonArray." }
            buildJsonObject {
                elementNames.forEachIndexed { index, name ->
                    put(name, child[index])
                }
            }
        }.let(::JsonArray)
    }
}
