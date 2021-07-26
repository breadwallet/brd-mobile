/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.websocket

import com.brd.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket

internal actual class WebSocketImpl actual constructor(url: String) {

    actual companion object {
        actual val implemented: Boolean = true
    }

    private val logger = Logger.create("[WebSocketImpl (${url})]")
    private val client = OkHttpClient().newBuilder().build()
    private val urlString = url
    private var webSocket: WebSocket? = null

    actual fun openSocket(listener: WebSocketListener) {
        val socketRequest = Request.Builder().url(urlString).build()
        webSocket = client.newWebSocket(
            socketRequest,
            object : okhttp3.WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    logger.debug("Connection opened! (protocol = ${response.protocol})")
                    listener.onOpen()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    logger.error("Connection error!", t)
                    listener.onFailure(t)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    logger.debug("Message received!", text)
                    listener.onMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    listener.onClosing(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    logger.debug("Connection closed! (code = $code, reason = $reason)")
                    listener.onClosed(code, reason)
                }
            }
        )
    }

    actual fun closeSocket(code: Int, reason: String) {
        webSocket?.close(code, reason)
        webSocket = null
    }

    actual fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}
