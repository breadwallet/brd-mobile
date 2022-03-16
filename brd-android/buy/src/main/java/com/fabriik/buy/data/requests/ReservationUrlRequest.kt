package com.fabriik.buy.data.requests

import com.squareup.moshi.Json

data class ReservationUrlRequest(
    @Json(name = "sourceAmount")
    val sourceAmount: String,

    @Json(name = "paymentMethod")
    val paymentMethod: String,

    @Json(name = "amountIncludeFees")
    val amountIncludeFees: Boolean,

    @Json(name = "sourceCurrency")
    val sourceCurrency: String,

    @Json(name = "destCurrency")
    val destCurrency: String,

    @Json(name = "redirectUrl")
    val redirectUrl: String,

    @Json(name = "failureRedirectUrl")
    val failureRedirectUrl: String,

    @Json(name = "referrerAccountId")
    val referrerAccountId: String,

    @Json(name = "dest")
    val dest: String,

    @Json(name = "country")
    val country: String
)