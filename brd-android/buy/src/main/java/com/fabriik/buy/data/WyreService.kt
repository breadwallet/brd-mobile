package com.fabriik.buy.data

import com.fabriik.buy.data.requests.ReservationUrlRequest
import com.fabriik.buy.data.responses.ReservationUrlResponse
import retrofit2.http.*

interface WyreService {

    @POST("/v3/orders/reserve")
    suspend fun getPaymentUrl(
        @Header("Authorization") auth: String,
        @Query("timestamp") timestamp: Long,
        @Body request: ReservationUrlRequest
    ) : ReservationUrlResponse
}