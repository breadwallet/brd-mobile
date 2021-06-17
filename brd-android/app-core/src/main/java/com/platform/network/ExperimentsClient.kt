/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 8/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.network

import android.content.Context
import android.util.Log
import com.breadwallet.model.Experiment
import com.breadwallet.tools.manager.BRReportsManager
import com.platform.APIClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Client responsible of interacting with the me/experiments endpoint from where we fetch feature flags.
 */
interface ExperimentsClient {

    /**
     * Fetch the list of the available experiments.
     */
    fun getExperiments(context: Context): List<Experiment>

}

object ExperimentsClientImpl : ExperimentsClient {
    private val TAG = ExperimentsClientImpl::class.java.simpleName

    private const val EXPERIMENTS_ENDPOINT = "/me/experiments"
    private const val JSON_ID = "id"
    private const val JSON_NAME = "name"
    private const val JSON_ACTIVE = "active"
    private const val JSON_META = "meta"

    override fun getExperiments(context: Context): List<Experiment> {
        val url = APIClient.getBaseURL() + EXPERIMENTS_ENDPOINT
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        val response = APIClient.getInstance(context).sendRequest(request, true)

        if (!response.isSuccessful) {
            Log.e(TAG, "Failed to fetch experiments: ${response.code}")
            return emptyList()
        }

        if (response.bodyText.isNullOrBlank()) {
            return emptyList()
        }

        return parseExperiments(response.bodyText)
    }

    private fun parseExperiments(responseBody: String): List<Experiment>  =
            try {
                val jsonArray = JSONArray(responseBody)
                List(jsonArray.length()) { parseExperiment(jsonArray.getJSONObject(it)) }
            } catch (e: JSONException) {
                Log.e(TAG, "Failed to parse experiments response", e)
                BRReportsManager.reportBug(e)
                emptyList()
            }

    private fun parseExperiment(messageJson: JSONObject) =
            Experiment(
                    messageJson.getInt(JSON_ID),
                    messageJson.getString(JSON_NAME),
                    messageJson.getBoolean(JSON_ACTIVE),
                    messageJson.optString(JSON_META))
}
