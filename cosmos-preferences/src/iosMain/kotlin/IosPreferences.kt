/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.prefs

import platform.Foundation.NSUserDefaults
import kotlin.native.concurrent.freeze

class IosPreferences(
    private val prefs: NSUserDefaults
) : Preferences {

    init {
        freeze()
    }

    @Suppress("UNCHECKED_CAST")
    override val keys: Set<String>
        get() = prefs.dictionaryRepresentation().keys as Set<String>

    override val size: Int
        get() = prefs.dictionaryRepresentation().size

    override fun contains(key: String): Boolean {
        return prefs.objectForKey(key) != null
    }

    override fun removeAll() {
        keys.forEach(prefs::removeObjectForKey)
    }

    override fun remove(key: String) {
        prefs.removeObjectForKey(key)
    }

    override fun putInt(key: String, value: Int) {
        prefs.setInteger(value.toLong(), key)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return if (contains(key)) prefs.integerForKey(key).toInt() else defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return if (contains(key)) prefs.integerForKey(key).toInt() else null
    }

    override fun putLong(key: String, value: Long) {
        prefs.setInteger(value, key)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return if (contains(key)) prefs.integerForKey(key) else defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return if (contains(key)) prefs.integerForKey(key) else null
    }

    override fun putString(key: String, value: String) {
        prefs.setObject(value, key)
    }

    override fun getString(key: String, defaultValue: String): String {
        return if (contains(key)) prefs.stringForKey(key) ?: defaultValue else defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return if (contains(key)) prefs.stringForKey(key) else null
    }

    override fun putDouble(key: String, value: Double) {
        prefs.setDouble(value, key)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return if (contains(key)) prefs.doubleForKey(key) else defaultValue
    }

    override fun getDoubleOrNull(name: String): Double? {
        return if (contains(name)) prefs.doubleForKey(name) else null
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.setBool(value, key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (contains(key)) prefs.boolForKey(key) else defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return if (contains(key)) prefs.boolForKey(key) else null
    }

    override fun putFloat(key: String, value: Float) {
        prefs.setFloat(value, key)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return if (contains(key)) prefs.floatForKey(key) else defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return if (contains(key)) prefs.floatForKey(key) else null
    }
}