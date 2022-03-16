package com.fabriik.buy.data.responses

import com.squareup.moshi.Json

data class ReservationUrlResponse(
    @Json(name = "url")
    val url: String,

    @Json(name = "reservation")
    val reservation: String
)