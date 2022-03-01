package com.breadwallet.breadbox

import com.breadwallet.appcore.BuildConfig

object FabriikApiConstants {

    val HOST = when {
        BuildConfig.DEBUG -> "one-dev.moneybutton.io/blocksatoshi"
        else -> "one-dev.moneybutton.io/blocksatoshi" //todo: change endpoint to production
    }

    private val BASE_URL = "https://${HOST}"

    private val HOST_WALLET_API = "$BASE_URL/wallet"
    private val HOST_BLOCKSATOSHI_API = "$BASE_URL/blocksatoshi"

    val ENDPOINT_CURRENCIES = "${HOST_WALLET_API}/currencies"
}