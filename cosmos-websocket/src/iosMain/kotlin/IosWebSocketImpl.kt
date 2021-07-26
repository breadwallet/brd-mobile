/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.websocket

//import com.brd.logger.Logger
//import platform.Foundation.*
//import platform.darwin.NSObject

// TODO: Requires new api with outdated OS target https://youtrack.jetbrains.com/issue/KT-37180
internal actual class WebSocketImpl actual constructor(url: String) {

    actual companion object {
        actual val implemented: Boolean = false
    }

    //private val logger = Logger.create("[WebSocketImpl (${url.takeLast(10)})]")
    //private val nsUrl = checkNotNull(NSURL.URLWithString(url))
    //private var webSocket: NSURLSessionWebSocketTask? = null

    actual fun openSocket(listener: WebSocketListener) {
        /*val delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
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
        }*/
    }

    /*private fun NSURLSessionWebSocketTask.handleMessages(listener: WebSocketListener) {
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
    }*/

    actual fun closeSocket(code: Int, reason: String) {
        //webSocket?.cancelWithCloseCode(code.toLong(), null)
        //webSocket = null
    }

    actual fun sendMessage(message: String) {
        /*webSocket?.apply {
            sendMessage(NSURLSessionWebSocketMessage(message)) { error ->
                error?.let {
                    logger.error("Failed to send message!", it, message)
                }
            }
        }*/
    }
}
