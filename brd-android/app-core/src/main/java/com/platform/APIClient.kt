/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
@file:Suppress(
    "ComplexMethod",
    "NestedBlockDepth",
    "TooManyFunctions",
    "ReturnCount",
    "MaxLineLength"
)

package com.platform

import android.accounts.*
import android.annotation.*
import android.content.*
import android.net.*
import android.os.*
import androidx.annotation.*
import com.brd.api.BrdApiClient
import com.blockset.walletkit.Key
import com.breadwallet.appcore.BuildConfig
import com.breadwallet.logger.*
import com.breadwallet.repository.*
import com.breadwallet.tools.animation.*
import com.breadwallet.tools.crypto.*
import com.breadwallet.tools.manager.*
import com.breadwallet.tools.security.*
import com.breadwallet.tools.util.*
import com.breadwallet.tools.util.BRConstants.DATE
import com.platform.tools.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.*
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.IOException
import java.lang.NumberFormatException
import java.lang.StringBuffer
import java.lang.System
import java.text.*
import java.text.ParseException
import java.util.*
import java.util.concurrent.atomic.*

private const val UNAUTHED_HTTP_STATUS = 401

class APIClient(
    private var context: Context,
    private val userManager: BrdUserManager,
    private val brdApiClient: BrdApiClient,
    private val okHttpClient: OkHttpClient,
    headers: Map<String, String>
) {

    private val httpHeaders: MutableMap<String, String> = mutableMapOf()

    init {
        httpHeaders.putAll(headers)
        httpHeaders[HEADER_ACCEPT_LANGUAGE] = getCurrentLanguageCode(context)
    }

    private var authKey: Key? = null
        get() {
            if (field == null) {
                val key = userManager.getAuthKey() ?: byteArrayOf()
                if (key.isNotEmpty()) {
                    field = Key.createFromPrivateKeyString(key).orNull()
                }
            }
            return field
        }

    private var mIsFetchingToken: Boolean = false

    private var mIsPlatformUpdating = false
    private val mItemsLeftToUpdate = AtomicInteger(0)

    /**
     * Return the current language code i.e. "en_US" for US English.
     */
    @TargetApi(Build.VERSION_CODES.N)
    private fun getCurrentLanguageCode(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0).toString()
        } else {
            // No inspection deprecation.
            context.resources.configuration.locale.toString()
        }
    }

    //only for testing
    fun buyBitcoinMe(): String? {
        if (UiUtils.isMainThread()) {
            throw NetworkOnMainThreadException()
        }
        val strUtl = getBaseURL() + ME
        val request = Request.Builder()
            .url(strUtl)
            .get()
            .build()
        val response = sendRequest(request, true)

        return response.bodyText
    }

    @VisibleForTesting
    fun sendHttpRequest(locRequest: Request, withAuth: Boolean, token: String?): Response? {
        check(!UiUtils.isMainThread()) { "urlGET: network on main thread" }

        val newBuilder = locRequest.newBuilder()
        for (key in httpHeaders.keys) {
            val value = httpHeaders[key]
            newBuilder.header(key, value!!)
        }

        //Add wallet rewards Id for signed requests
        if (withAuth) {
            val walletId = BRSharedPrefs.getWalletRewardId()
            if (!walletId.isNullOrEmpty()) {
                newBuilder.addHeader(HEADER_WALLET_ID, walletId)
            }
        }

        var request: Request = newBuilder.build()
        if (withAuth) {
            request = authenticateRequest(request, token) ?: return null
        }

        val rawResponse: Response
        try {
            rawResponse = okHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            logError("sendRequest: ", e)
            val message = e.message ?: ""
            return Response.Builder()
                .code(NETWORK_ERROR_CODE)
                .request(request)
                .body(message.toResponseBody(null))
                .message(message)
                .protocol(Protocol.HTTP_1_1)
                .build()
        }

        val bytesBody = try {
            rawResponse.body?.bytes() ?: byteArrayOf()
        } catch (e: IOException) {
            logError("sendHttpRequest: ", e)
            BRReportsManager.reportBug(e)
            byteArrayOf()
        }

        if (bytesBody.isEmpty()) {
            return createNewResponseWithBody(rawResponse, bytesBody)
        }

        val contentEncoding = rawResponse.header(BRConstants.CONTENT_ENCODING, "")
        return createNewResponseWithBody(
            rawResponse,
            when {
                contentEncoding.equals(BRConstants.GZIP, true) -> {
                    logDebug("the content is gzip, unzipping")
                    val decompressed = BRCompressor.gZipExtract(bytesBody)
                    if (decompressed == null) {
                        BRReportsManager.reportBug(IllegalArgumentException("failed to decrypt data!"))
                    }
                    decompressed
                }
                else -> bytesBody
            }
        )
    }

    private fun createNewResponseWithBody(response: Response, body: ByteArray?): Response {
        val postReqBody = (body ?: byteArrayOf()).toResponseBody()
        return response.newBuilder().body(postReqBody).build()
    }

    fun sendRequest(request: Request, withAuth: Boolean): BRResponse {
        val tokenUsed = if (withAuth) TokenHolder.retrieveToken() else null
        sendHttpRequest(request, withAuth, tokenUsed).use { response ->
            if (response == null) {
                BRReportsManager.reportBug(AuthenticatorException("Request: ${request.url} response is null"))
                return BRResponse()
            }
            if (response.code == UNAUTHED_HTTP_STATUS) {
                BRReportsManager.reportBug(AuthenticatorException("Request: ${request.url} returned 401!"))
            }
            if (!response.isSuccessful) {
                logRequestAndResponse(request, response)
            }
            if (response.isRedirect) {
                val newLocation =
                    request.url.scheme + "://" + request.url.host + response.header("location")
                val newUri = Uri.parse(newLocation)
                if (newUri == null) {
                    logError("redirect uri is null")
                    return createBrResponse(response)
                } else if (!BuildConfig.DEBUG &&
                    (!newUri.host!!.equals(host, true) ||
                        !newUri.scheme!!.equals(PROTO, true))
                ) {
                    logError("WARNING: redirect is NOT safe: $newLocation")
                    return createBrResponse(
                        Response.Builder()
                            .code(500)
                            .request(request)
                            .body(ByteArray(0).toResponseBody(null))
                            .message("")
                            .protocol(Protocol.HTTP_1_1)
                            .build()
                    )
                } else {
                    logWarning("redirecting: ${request.url} >>> $newLocation")
                    return createBrResponse(
                        sendHttpRequest(
                            Request.Builder().url(newLocation).get().build(),
                            withAuth,
                            tokenUsed
                        )
                    )
                }
            } else if (withAuth && isBreadChallenge(response)) {
                logDebug("got authentication challenge from API - will attempt to get token, url -> ${request.url}")
                val newToken = TokenHolder.updateToken(tokenUsed)
                return if (newToken == null) {
                    logError("Token update failed, will not re-attempt")
                    BRResponse()
                } else {
                    logInfo("Token updated, retrying")
                    createBrResponse(sendHttpRequest(request, true, newToken))
                }
            }
            return createBrResponse(response)
        }
    }

    private fun createBrResponse(res: Response?): BRResponse {
        var brRsp = BRResponse()
        try {
            if (res != null) {
                val code = res.code
                val headers = HashMap<String, String>()
                for (name in res.headers.names()) {
                    headers[name.lowercase()] = res.header(name)!!
                    if (name.equals(DATE, true)) {
                        res.header(name)
                            ?.run(::parseGmtDate)
                            ?.time
                            ?.run(BRSharedPrefs::putSecureTime)
                    }
                }

                var bytesBody: ByteArray? = null
                var contentType = headers[HEADER_CONTENT_TYPE]
                try {
                    val body = res.body
                    if (contentType == null) {
                        contentType =
                            if (body!!.contentType() != null) body.contentType()!!.type else ""
                    }
                    bytesBody = body!!.bytes()
                } catch (ex: IOException) {
                    logError("createBrResponse: ", ex)
                } finally {
                    res.close()
                }
                brRsp =
                    BRResponse(bytesBody, code, headers, res.request.url.toString(), contentType)
            }
        } finally {
            if (!brRsp.isSuccessful) {
                brRsp.print()
            }
        }
        return brRsp
    }

    private fun authenticateRequest(request: Request, token: String?): Request? {
        val modifiedRequest: Request.Builder = request.newBuilder()
        val body = request.body

        val base58Body = try {
            if (body != null && body.contentLength() != 0L) {
                val sink = Buffer()
                try {
                    body.writeTo(sink)
                } catch (e: IOException) {
                    logError("authenticateRequest: ", e)
                }

                val bytes = sink.buffer.readByteArray()
                CryptoHelper.base58ofSha256(bytes)
            } else ""
        } catch (e: IOException) {
            logError("authenticateRequest: ", e)
            ""
        }

        val httpDate = formatGmtDate(Date())
            ?.run { substring(0, indexOf(GMT) + GMT.length) }
            ?.also { httpDate ->
                modifiedRequest.header(DATE, httpDate)
            }

        val queryString = request.url.encodedQuery
        val url = request.url.encodedPath + if (queryString.isNullOrBlank()) "" else "?$queryString"

        val requestString = """
            ${request.method}
            $base58Body
            ${request.header(BRConstants.HEADER_CONTENT_TYPE).orEmpty()}
            $httpDate
            $url
        """.trimIndent()

        val signedRequest = signRequest(requestString, authKey ?: return null) ?: return null
        val clientToken = brdApiClient.brdAuthProvider.clientToken()
        val authValue = if (clientToken == null) {
            "$BREAD $token:$signedRequest"
        } else {
            "$BREAD2 $clientToken:$token:$signedRequest"
        }
        return modifiedRequest
            .header(BRConstants.AUTHORIZATION, authValue)
            .build()
    }

    private fun isBreadChallenge(resp: Response): Boolean {
        val challenge = resp.header(BRConstants.HEADER_WWW_AUTHENTICATE)
        return (challenge != null && challenge.startsWith(BREAD)) || resp.code == 403
    }

    fun buildUrl(path: String): String {
        return getBaseURL() + path
    }

    /**
     * Launch in separate threads updates for bundles, feature flags, KVStore entries and fees.
     */
    fun updatePlatform(scope: CoroutineScope) {
        if (mIsPlatformUpdating) {
            logError("updatePlatform: platform already Updating!")
            return
        }
        mIsPlatformUpdating = true

        //update Bundle
        scope.launch {
            val startTime = System.currentTimeMillis()
            ServerBundlesHelper.updateBundles(context)
            val endTime = System.currentTimeMillis()
            val bundle = ServerBundlesHelper.getBundle(ServerBundlesHelper.Type.WEB)
            logDebug("updateBundles $bundle: DONE in ${endTime - startTime}ms")
            itemFinished()
        }

        //update feature flags
        scope.launch {
            val startTime = System.currentTimeMillis()
            ExperimentsRepositoryImpl.refreshExperiments(context)
            val endTime = System.currentTimeMillis()
            logDebug("updateFeatureFlag: DONE in " + (endTime - startTime) + "ms")
            itemFinished()
        }
    }

    private fun itemFinished() {
        val items = mItemsLeftToUpdate.incrementAndGet()
        if (items >= SYNC_ITEMS_COUNT) {
            logDebug("PLATFORM ALL UPDATED: $items")
            mIsPlatformUpdating = false
            mItemsLeftToUpdate.set(0)
        }
    }

    class BRResponse constructor(
        var body: ByteArray? = byteArrayOf(),
        var code: Int = 0,
        private val mHeaders: Map<String, String>? = null,
        val url: String? = "",
        var contentType: String? = ""
    ) {
        val headers: Map<String, String>
            get() = mHeaders ?: HashMap()

        val bodyText: String
            get() = body?.run { toString(Charsets.UTF_8) } ?: ""

        val isSuccessful: Boolean
            get() = code in 200..299

        init {
            if (contentType.isNullOrBlank()) {
                if (mHeaders != null && mHeaders.containsKey(BRConstants.HEADER_CONTENT_TYPE)) {
                    this.contentType = mHeaders[BRConstants.HEADER_CONTENT_TYPE]
                    if (contentType.isNullOrBlank()) {
                        this.contentType = BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8
                    }
                }
            }
        }

        constructor(body: ByteArray?, code: Int, contentType: String?) : this(
            body,
            code,
            null,
            null,
            contentType
        )

        constructor(contentType: String?, code: Int) : this(null, code, null, null, contentType)

        fun print() {
            val logText = String.format("%s (%d)|%s|", url, code, bodyText)
            if (isSuccessful) {
                logDebug(logText)
            } else {
                logError(logText)
            }
        }
    }

    private fun logRequestAndResponse(request: Request, response: Response) {
        val reportStringBuffer = StringBuffer()
        reportStringBuffer.append("Request:\n")
        reportStringBuffer.append(request.url)
        reportStringBuffer.append("\n")
        reportStringBuffer.append(request.headers.toString())
        reportStringBuffer.append(bodyToString(request))
        reportStringBuffer.append("\n\n")
        reportStringBuffer.append("Response:\n")
        reportStringBuffer.append(response.code)
        reportStringBuffer.append(response.message)
        reportStringBuffer.append("\n")
        reportStringBuffer.append(response.headers.toString())
        reportStringBuffer.append("\n")
        logError("Not successful: \n$reportStringBuffer")
    }

    companion object {
        // proto is the transport protocol to use for talking to the API (either http or https)
        private const val PROTO = "https"
        private const val HTTPS_SCHEME = "https://"
        private const val GMT = "GMT"

        @VisibleForTesting
        const val BREAD = "bread"
        const val BREAD2 = "bread2"
        private const val NETWORK_ERROR_CODE = 599
        private const val SYNC_ITEMS_COUNT = 4
        private const val FEATURE_FLAG_PATH = "/me/features"
        private const val PUBKEY = "pubKey"
        private const val DEVICE_ID = "deviceID"

        // convenience getter for the API endpoint
        private val BASE_URL
            get() = if(!host.startsWith(HTTPS_SCHEME)) HTTPS_SCHEME + host else host

        //Fee per kb url
        private const val FEE_PER_KB_URL = "/v1/fee-per-kb"

        //token path
        private const val TOKEN_PATH = "/token"
        private const val TOKEN = "token"

        //me path
        private const val ME = "/me"

        // Http Header constants
        private const val HEADER_WALLET_ID = "X-Wallet-Id"
        const val HEADER_IS_INTERNAL = "X-Is-Internal"
        const val HEADER_TESTFLIGHT = "X-Testflight"
        const val HEADER_TESTNET = "X-Bitcoin-Testnet"
        private const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        const val HEADER_USER_AGENT = "User-agent"
        private const val HEADER_CONTENT_TYPE = "content-type"

        // User Agent constants
        const val SYSTEM_PROPERTY_USER_AGENT = "http.agent"
        const val UA_APP_NAME = "breadwallet/"
        const val UA_PLATFORM = "android/"

        private val DATE_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone(GMT) }
        private const val CONNECTION_TIMEOUT_SECONDS = 30

        fun parseGmtDate(dateString: String): Date? {
            return try {
                DATE_FORMAT.parse(dateString)
            } catch (e: ParseException) {
                null
            } catch (e: NumberFormatException) {
                // occurs occasionally on some devices running Android 10
                null
            }
        }

        fun formatGmtDate(date: Date): String? {
            return DATE_FORMAT.format(date)
        }

        @JvmStatic
        @Synchronized
        @Deprecated("Retrieve from the Application Kodein instance.")
        fun getInstance(context: Context): APIClient {
            val kodein by closestDI(context)
            return kodein.direct.instance()
        }

        val host: String
            get() {
                if (BuildConfig.DEBUG) {
                    val host = BRSharedPrefs.getDebugHost()
                    if (!host.isNullOrBlank()) {
                        return host
                    }
                }
                return BRSharedPrefs.getApiHostString()
            }

        @JvmStatic
        fun getBaseURL(): String {
            // In the debug case, the user may have changed the host.
            if (BuildConfig.DEBUG) {
                return if (host.startsWith("http")) host else HTTPS_SCHEME + host
            }
            return BASE_URL
        }

        /**
         * Convert [Request] to a [String].
         *
         *
         * Reference: [stackoverflow](https://stackoverflow.com/a/29033727/3211679)
         *
         * @param request The request to convert to a [String].
         * @return The [String] version of the specified [Request].
         */

        private fun bodyToString(request: Request): String? {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                val body = copy.body
                body?.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                null
            }
        }

        fun signRequest(request: String, key: Key): String? {
            val requestHash = CryptoHelper.doubleSha256(request.toByteArray())
            if (requestHash == null) {
                logError("requestHash is null, cannot sign request")
                return null
            }

            val signedBytes = CryptoHelper.signCompact(requestHash, key)
            if (signedBytes.isEmpty()) {
                logError("Failed to sign request.")
                return null
            }
            return CryptoHelper.base58Encode(signedBytes)
        }
    }
}
