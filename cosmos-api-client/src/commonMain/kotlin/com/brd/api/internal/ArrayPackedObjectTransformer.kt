/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
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
package com.brd.api.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

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
@Suppress("UNCHECKED_CAST")
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
