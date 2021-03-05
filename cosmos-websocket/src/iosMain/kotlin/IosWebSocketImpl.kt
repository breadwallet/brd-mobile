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
import platform.Foundation.*
import platform.darwin.NSObject

internal actual class WebSocketImpl actual constructor(url: String) {

    private val logger = Logger.create("[WebSocketImpl (${url.takeLast(10)})]")
    private val nsUrl = checkNotNull(NSURL.URLWithString(url))
    private var webSocket: NSURLSessionWebSocketTask? = null

    actual fun openSocket(listener: WebSocketListener) {
        val delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didOpenWithProtocol: String?
            ) {
                logger.debug("Connection opened! (protocol = $didOpenWithProtocol)")
                listener.onOpen()
            }

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didCloseWithCode: NSURLSessionWebSocketCloseCode,
                reason: NSData?
            ) {
                logger.debug("Connection closed! (code = $didCloseWithCode, reason = $reason)")
                listener.onClosed(didCloseWithCode.toInt(), reason.toString())
            }
        }
        val urlSession = NSURLSession.sessionWithConfiguration(
            delegateQueue = NSOperationQueue.currentQueue(),
            configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
            delegate = delegate
        )
        webSocket = urlSession.webSocketTaskWithURL(nsUrl).apply {
            handleMessages(listener)
            resume()
        }
    }

    private fun NSURLSessionWebSocketTask.handleMessages(listener: WebSocketListener) {
        receiveMessageWithCompletionHandler { message, nsError ->
            when {
                nsError != null -> {
                    val e = Throwable(nsError.description)
                    logger.error("Connection error!", e)
                    listener.onFailure(e)
                }
                message != null -> {
                    logger.debug("Message received!", message)
                    message.string?.let(listener::onMessage)
                }
            }
            handleMessages(listener)
        }
    }

    actual fun closeSocket(code: Int, reason: String) {
        webSocket?.cancelWithCloseCode(code.toLong(), null)
        webSocket = null
    }

    actual fun sendMessage(message: String) {
        webSocket?.apply {
            sendMessage(NSURLSessionWebSocketMessage(message)) { error ->
                error?.let {
                    logger.error("Failed to send message!", it, message)
                }
            }
        }
    }
}