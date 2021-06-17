/**
 * BreadWallet
 *
 * Created by Ahsan Butt on <ahsan.butt@breadwallet.com> 7/14/2020.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.webkit.JavascriptInterface
import com.breadwallet.tools.util.CUSTOM_DATA_KEY_TITLE
import com.breadwallet.tools.util.DebugInfo
import com.breadwallet.tools.util.SupportManager
import org.json.JSONException
import org.json.JSONObject

private const val SUPPORT_SUBJECT = "BRD Order Inquiry"
private const val SUPPORT_TEXT = "[Please add any further information about your order that you wish here, otherwise all you need to do is send this email. BRD Support will assist you as soon as possible.]"

class SupportJs(
    private val promise: NativePromiseFactory,
    private  val supportManager: SupportManager
) : JsApi {

    @JavascriptInterface
    fun submitRequest(
        debugData: String
    ) = promise.createForUnit {
        val debugJson = try {
            JSONObject(debugData)
        } catch (e: JSONException) {
            JSONObject()
        }
        val debugMap = mutableMapOf<String, String>()
        debugMap[CUSTOM_DATA_KEY_TITLE] = "Order Details"
        debugJson.keys().forEach {
            debugMap[it] = debugJson.getString(it)
        }
        supportManager.submitEmailRequest(
            subject = SUPPORT_SUBJECT,
            body =  SUPPORT_TEXT,
            diagnostics = listOf(DebugInfo.CUSTOM),
            customData = debugMap,
            attachLogs = false
        )
    }
}