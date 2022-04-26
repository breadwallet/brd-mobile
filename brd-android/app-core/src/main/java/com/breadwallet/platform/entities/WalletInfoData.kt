package com.breadwallet.platform.entities

import com.blockset.walletkit.WalletManagerMode
import com.breadwallet.logger.logError
import com.platform.util.getIntOrDefault
import com.platform.util.getLongOrDefault
import com.platform.util.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/22/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
data class WalletInfoData(
    val classVersion: Int = DEFAULT_CLASS_VERSION,
    val creationDate: Long = DEFAULT_CREATION_DATE,
    val name: String? = null,
    val connectionModes: Map<String, WalletManagerMode> = emptyMap()
) {
    companion object {
        private const val NAME = "name"
        private const val CLASS_VERSION = "classVersion"
        private const val CREATION_DATE = "creationDate"
        private const val CONNECTION_MODES = "connectionModes"

        private const val DEFAULT_CLASS_VERSION = 3
        private const val DEFAULT_CREATION_DATE = 0L

        fun fromJsonObject(json: JSONObject): WalletInfoData = json.run {
            WalletInfoData(
                classVersion = getIntOrDefault(CLASS_VERSION, DEFAULT_CLASS_VERSION),
                creationDate = getLongOrDefault(CREATION_DATE, DEFAULT_CREATION_DATE),
                name = getStringOrNull(NAME),
                connectionModes = getConnectionModes(this)
            )
        }

        private fun getConnectionModes(json: JSONObject): Map<String, WalletManagerMode> {
            val mutableModes = mutableMapOf<String, WalletManagerMode>()
            val modes = json.optJSONArray(CONNECTION_MODES) ?: return mutableModes.toMap()
            try {
                var currencyId = ""
                for (i in 0 until modes.length()) {
                    if (i % 2 == 0) currencyId = modes.getString(i)
                    else mutableModes[currencyId] =
                        WalletManagerMode.fromSerialization(modes.getInt(i))
                }
            } catch (ex: JSONException) {
                logError("Malformed $CONNECTION_MODES array: $modes")
            }

            return mutableModes.toMap()
        }
    }

    fun toJSON(): JSONObject {
        val connectionModesList = mutableListOf<Any>()
        connectionModes.entries.forEach {
            connectionModesList.add(it.key)
            connectionModesList.add(it.value.toSerialization())
        }

        return JSONObject(
            mapOf(
                CLASS_VERSION to classVersion,
                CREATION_DATE to creationDate,
                NAME to name,
                CONNECTION_MODES to connectionModesList
            )
        )
    }
}
