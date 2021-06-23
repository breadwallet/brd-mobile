/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.util

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.getJSONObjectOrNull(name: String): JSONObject? =
    if (has(name) && !isNull(name)) getJSONObject(name) else null

fun JSONObject.getJSONArrayOrNull(name: String): JSONArray? =
    if (has(name) && !isNull(name)) getJSONArray(name) else null

/** Returns the value mapped by name of null if it doesn't exist. */
fun JSONObject.getStringOrNull(name: String): String? =
    if (has(name) && !isNull(name)) getString(name) else null

/** Returns the value mapped by name or [default] if it doesn't exist. */
fun JSONObject.getBooleanOrDefault(name: String, default: Boolean) =
    if (has(name) && !isNull(name)) getBoolean(name) else default

/** Returns the value mapped by name or [default] if it doesn't exist. */
fun JSONObject.getIntOrDefault(name: String, default: Int = 0) =
    if (has(name) && !isNull(name)) getInt(name) else default

/** Returns the value mapped by name or [default] if it doesn't exist. */
fun JSONObject.getLongOrDefault(name: String, default: Long = 0) =
    if (has(name) && !isNull(name)) getLong(name) else default

/** Returns the value mapped by name of null if it doesn't exist. */
fun JSONObject.getDoubleOrNull(name: String): Double? =
    if (has(name) && !isNull(name)) getDouble(name) else null

/** Returns the value mapped by name or [default] if it doesn't exist. */
fun JSONObject.getDoubleOrDefault(name: String, default: Double = 0.0) =
    if (has(name) && !isNull(name)) getDouble(name) else default

/** Returns the value mapped by name or [default] if it doesn't exist or is malformed. */
fun JSONObject.getDoubleOrDefaultSafe(name: String, default: Double = 0.0) =
    try {
        getDoubleOrDefault(name, default)
    } catch (e: Exception) {
        default
    }

/** Returns the value at [index] or null. */
fun JSONArray.getStringOrNull(index: Int) =
    if (index in 0 until length()) getString(index) else null
