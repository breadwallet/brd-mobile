package com.breadwallet.breadbox

import com.breadwallet.appcore.BuildConfig

object FabriikApiConstants {

    private val HOST = when {
        BuildConfig.DEBUG -> "https://one-dev.moneybutton.io/blocksatoshi/"
        else -> "https://one-dev.moneybutton.io/blocksatoshi/" //todo: change endpoint to production
    }

    private val HOST_WALLET_API = HOST + "wallet"
    private val HOST_BLOCKSATOSHI_API = HOST + "blocksatoshi"

    val ENDPOINT_CURRENCIES = "${HOST_WALLET_API}/currencies"
}