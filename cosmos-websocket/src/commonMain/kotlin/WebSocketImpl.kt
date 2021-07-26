/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.websocket

internal expect class WebSocketImpl(url: String) {
    companion object {
        val implemented: Boolean
    }

    fun openSocket(listener: WebSocketListener)
    fun closeSocket(code: Int, reason: String)
    fun sendMessage(message: String)
}

interface WebSocketListener {
    fun onOpen()
    fun onFailure(throwable: Throwable)
    fun onMessage(message: String)
    fun onClosing(code: Int, reason: String)
    fun onClosed(code: Int, reason: String)
}
