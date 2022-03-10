package com.breadwallet.breadbox

import com.breadwallet.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class FabriikAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!chain.request().url.toString().startsWith(FabriikApiConstants.HOST_BLOCKSATOSHI_API)) {
            return chain.proceed(chain.request())
        }

        return chain.request()
            .newBuilder()
            .addHeader("Authorization", BuildConfig.FABRIIC_CLIENT_TOKEN)
            .build()
            .run(chain::proceed)
    }
}