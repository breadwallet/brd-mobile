/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> 7/17/18.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.protocols.messageexchange.entities

class EncryptedMessage(val encryptedData: ByteArray, val nonce: ByteArray)