/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 7/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.network

import android.content.Context
import android.util.Log
import com.breadwallet.BuildConfig
import com.breadwallet.tools.util.BRConstants
import com.platform.APIClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject

/**
 * Client responsible of registering and unregistering the Firebase token for push notifications
 * in our backend.
 */
interface NotificationsSettingsClient {

    /**
     * Send push notifications token. Return True if the token was successfully registered,
     * otherwise false.
     */
    fun registerToken(context: Context, token: String): Boolean

    /**
     * Remove push notifications token. Return True if the token was successfully unregistered,
     * otherwise false.
     */
    fun unregisterToken(context: Context, token: String): Boolean

}

object NotificationsSettingsClientImpl : NotificationsSettingsClient {

    private val TAG = NotificationsSettingsClientImpl::class.java.simpleName
    private const val ENDPOINT_PUSH_DEVICES = "/me/push-devices"
    private const val ENDPOINT_DELETE_PUSH_DEVICES = "/me/push-devices/apns/"
    private const val PUSH_SERVICE = "fcm"
    private const val KEY_TOKEN = "token"
    private const val KEY_SERVICE = "service"
    private const val KEY_DATA = "data"
    private const val KEY_DATA_ENVIRONMENT = "e"
    private const val KEY_DATA_BUNDLE_ID = "b"
    private const val ENVIRONMENT_DEVELOPMENT = "d"
    private const val ENVIRONMENT_PRODUCTION = "p"

    override fun registerToken(context: Context, token: String): Boolean {
        val url = APIClient.getBaseURL() + ENDPOINT_PUSH_DEVICES
        val deviceEnvironment = if (BuildConfig.DEBUG) {
            ENVIRONMENT_DEVELOPMENT
        } else {
            ENVIRONMENT_PRODUCTION
        }

        try {
            val payload = JSONObject()
            payload.put(KEY_TOKEN, token)
            payload.put(KEY_SERVICE, PUSH_SERVICE)

            val data = JSONObject()
            data.put(KEY_DATA_ENVIRONMENT, deviceEnvironment)
            data.put(KEY_DATA_BUNDLE_ID, context.packageName)
            payload.put(KEY_DATA, data)

            val json = BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8.toMediaTypeOrNull()

            val requestBody = payload.toString().toRequestBody(json)

            val request = Request.Builder()
                    .url(url)
                    .header(BRConstants.HEADER_CONTENT_TYPE, BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8)
                    .header(BRConstants.HEADER_ACCEPT, BRConstants.CONTENT_TYPE_JSON).post(requestBody).build()

            val response = APIClient.getInstance(context).sendRequest(request, true)
            return response.isSuccessful

        } catch (e: JSONException) {
            Log.e(TAG, "Error constructing JSON payload while updating FCM registration token.", e)
        }
        return false
    }

    override fun unregisterToken(context: Context, token: String): Boolean {
        val url = APIClient.getBaseURL() + ENDPOINT_DELETE_PUSH_DEVICES + token
        val request = Request.Builder()
                .url(url)
                .delete()
                .build()
        val response = APIClient.getInstance(context).sendRequest(request, true)
        return response.isSuccessful
    }

}
