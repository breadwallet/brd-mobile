/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 6/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * In-app message model.
 */
@Parcelize
data class Message(val messageId: Long,
                   val mixpanelId: Long,
                   val title: String,
                   val body: String,
                   val imageUrl: String,
                   val cta: String,
                   val ctaUrl: String): Parcelable