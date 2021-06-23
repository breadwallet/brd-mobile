/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on on 6/22/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.entities

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TokenListMetaData(
    enabledCurrenciesList: List<TokenInfo> = listOf(),
    hiddenCurrenciesList: List<TokenInfo> = listOf()
) {
    var enabledCurrencies = mutableListOf<TokenInfo>()
    var hiddenCurrencies = mutableListOf<TokenInfo>()

    companion object {
        private const val ENABLED_CURRENCIES = "enabledCurrencies"
        private const val HIDDEN_CURRENCIES = "hiddenCurrencies"
    }

    init {
        enabledCurrencies.addAll(enabledCurrenciesList)
        hiddenCurrencies.addAll(hiddenCurrenciesList)
    }

    constructor(json: JSONObject) : this() {
        enabledCurrencies = jsonToMetaData(json.getJSONArray(ENABLED_CURRENCIES))
        hiddenCurrencies = jsonToMetaData(json.getJSONArray(HIDDEN_CURRENCIES))
    }

    @Throws(JSONException::class)
    private fun jsonToMetaData(json: JSONArray): MutableList<TokenInfo> =
        MutableList(json.length()) {
            val tokenStr = json.getString(it)
            when {
                tokenStr.contains(":") -> {
                    val parts =
                        tokenStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    TokenInfo(parts[0], true, parts[1])
                }
                else -> TokenInfo(tokenStr, false, null)
            }
        }

    data class TokenInfo(var symbol: String, var erc20: Boolean, var contractAddress: String?) {
        override fun toString() = when {
            erc20 -> "$symbol:$contractAddress"
            else -> symbol
        }
    }

    /**
     * TokenListMetaData:
     *
     *
     * Key: “token-list-metadata”
     *
     *
     * {
     * “classVersion”: 2, //used for versioning the schema
     * "enabledCurrencies": ["btc":"eth": "erc20:0xsd98fjetc"] enabled currencies
     * "hiddenCurrencies": "bch"] hidden currencies
     * }
     */
}
