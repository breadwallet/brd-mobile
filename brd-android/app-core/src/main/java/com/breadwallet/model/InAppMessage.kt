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
data class InAppMessage(
        val id: String,
        val type: Type,
        val messageId: String,
        val title: String,
        val body: String,
        val actionButtonText: String?,
        val actionButtonUrl: String?,
        val imageUrl: String?
) : Parcelable {

    enum class Type {
        IN_APP_NOTIFICATION, // Notifications that will be shown while the user is using the app.
        UNKNOWN;

        companion object {
            fun fromString(value: String): Type = when (value) {
                "inApp" -> IN_APP_NOTIFICATION
                else -> UNKNOWN
            }
        }
    }
}