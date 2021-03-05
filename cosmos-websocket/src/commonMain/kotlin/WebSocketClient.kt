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