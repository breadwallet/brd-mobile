/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.prefs

interface Preferences {

    val keys: Set<String>
    val size: Int

    fun contains(key: String): Boolean

    fun removeAll()
    fun remove(key: String)

    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun getIntOrNull(key: String): Int?

    fun putLong(key: String, value: Long)
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun getLongOrNull(key: String): Long?

    fun putFloat(key: String, value: Float)
    fun getFloat(key: String, defaultValue: Float = 0f): Float
    fun getFloatOrNull(key: String): Float?

    fun putDouble(key: String, value: Double)
    fun getDouble(key: String, defaultValue: Double = 0.0): Double
    fun getDoubleOrNull(key: String): Double?

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun getBooleanOrNull(key: String): Boolean?

    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String = ""): String
    fun getStringOrNull(key: String): String?
}
