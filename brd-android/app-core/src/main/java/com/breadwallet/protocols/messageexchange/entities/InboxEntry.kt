/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail></mihail>@breadwallet.com> 7/13/18.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.protocols.messageexchange.entities

class InboxEntry(
    val receivedTime: String,
    val isAcknowledged: Boolean,
    val acknowledgedTime: String,   //Envelope with encrypted message
    val message: String,
    val cursor: String,
    val serviceUrl: String
)