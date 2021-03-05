/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brd.websocket

import com.brd.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket

internal actual class WebSocketImpl actual constructor(url: String) {

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