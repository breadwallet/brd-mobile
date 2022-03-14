package com.fabriik.buy.data

import com.fabriik.buy.data.requests.ReservationUrlRequest
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WyreApi(private val service: WyreService) {

    private val secretKey = "SK-DH6XP4AX-ZQ3ULQ2G-6CVP3PF9-TFJ4N7VJ" //todo: move secret key

    suspend fun getPaymentUrl() = service.getPaymentUrl(
        auth = "Bearer $secretKey",
        timestamp = System.currentTimeMillis(),
        request = ReservationUrlRequest(
            sourceAmount = "10",
            paymentMethod = "debit-card",
            amountIncludeFees = true,
            sourceCurrency = "USD",
            destCurrency = "ETH",
            redirectUrl = "https://www.sendwyre.com",
            failureRedirectUrl = "https://www.sendwyre.com",
            referrerAccountId = "AC_T6HMDWDGM8V", // todo: move account id
            dest = "ethereum:0x9E01E0E60dF079136a7a1d4ed97d709D5Fe3e341",
            country = "US"
        )
    )

    companion object {

        fun create() = WyreApi(
            Retrofit.Builder()
                .baseUrl("https://api.testwyre.com") //todo: change env
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(WyreService::class.java)
        )
    }
}