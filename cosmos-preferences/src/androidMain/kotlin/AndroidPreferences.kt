/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.prefs

import android.annotation.SuppressLint
import android.content.SharedPreferences

class AndroidPreferences(
    private val prefs: SharedPreferences,
) : Preferences {

    override val keys: Set<String>
        get() = prefs.all.keys.toSet()

    override val size: Int
        get() = prefs.all.size

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun removeAll() {
        prefs.edit { clear() }
    }

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    override fun getIntOrNull(key: String): Int? {
        return if (contains(key)) prefs.getInt(key, 0) else null
    }

    override fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    override fun getLongOrNull(key: String): Long? {
        return if (contains(key)) prefs.getLong(key, 0L) else null
    }

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return if (contains(key)) prefs.getString(key, "") else null
    }

    override fun putDouble(key: String, value: Double) {
        putLong(key, value.toRawBits())
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))
    }

    override fun getDoubleOrNull(key: String): Double? {
        return if (contains(key)) getDouble(key, 0.0) else null
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return if (contains(key)) prefs.getBoolean(key, false) else null
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return prefs.getFloat(key, defaultValue)
    }

    override fun putFloat(key: String, value: Float) {
        prefs.edit { putFloat(key, value) }
    }

    override fun getFloatOrNull(key: String): Float? {
        return if (contains(key)) prefs.getFloat(key, 0f) else null
    }

    @SuppressLint("ApplySharedPref")
    private inline fun SharedPreferences.edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        val editor = edit()
        action(editor)
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }
}
