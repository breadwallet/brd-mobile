/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.websocket

class WebSocketClient(url: String) {

    private val ws = WebSocketImpl(url)
    private val socketListener = object : WebSocketListener {
        override fun onOpen() {
            currentState = State.CONNECTED
        }

        override fun onFailure(throwable: Throwable) {
            socketError = throwable
            currentState = State.CLOSED
        }

        override fun onMessage(message: String) {
            messageListener?.invoke(message)
        }

        override fun onClosing(code: Int, reason: String) {
            currentState = State.CLOSING
        }

        override fun onClosed(code: Int, reason: String) {
            currentState = State.CLOSED
        }
    }

    var socketError: Throwable? = null
        private set
    var currentState: State = State.CLOSED
        private set(value) {
            field = value
            stateListener?.invoke(value)
        }
    var stateListener: ((State) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(currentState)
        }
    var messageListener: ((msg: String) -> Unit)? = null

    fun connect() {
        check(currentState == State.CLOSED) {
            "Failed to connect, connection is not closed!"
        }
        socketError = null
        currentState = State.CONNECTING
        ws.openSocket(socketListener)
    }

    fun disconnect() {
        if (currentState != State.CLOSED) {
            currentState = State.CLOSING
            ws.closeSocket(1000, "The user has closed the connection.")
        }
    }

    fun send(msg: String) {
        check(currentState == State.CONNECTED) {
            "Message rejected, client is not connected!"
        }
        ws.sendMessage(msg)
    }

    enum class State {
        CONNECTING,
        CONNECTED,
        CLOSING,
        CLOSED
    }
}