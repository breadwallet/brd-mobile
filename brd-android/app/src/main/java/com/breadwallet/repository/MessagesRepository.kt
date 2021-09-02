/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 6/7/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.repository

import android.content.Context
import android.util.Log
import com.breadwallet.model.InAppMessage
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.util.EventUtils
import com.platform.network.InAppMessagesClient

/**
 * Repository for in app messages. Provides methods for fetching new messages and to notify that the
 * message was read.
 */
object MessagesRepository {
    private val TAG = MessagesRepository::class.java.simpleName

    /**
     * Fetch latest in app notification.
     */
    fun getInAppNotification(context: Context): InAppMessage? {
        Log.d(TAG, "getInAppNotification: Looking for new in app notifications")
        val readMessages = BRSharedPrefs.getReadInAppNotificationIds()
        // Filter any notification that we already shown
        val inAppMessages = InAppMessagesClient.fetchMessages(context, InAppMessage.Type.IN_APP_NOTIFICATION)
            .filterNot { readMessages.contains(it.messageId) }

        if (inAppMessages.isEmpty()) {
            Log.d(TAG, "getInAppNotification: There are no new notifications")
            return null
        }
        // We are not suppose to get more than one in app notification from the backend at the same
        // time but in case it happens we pick the first and will show the others next time we check
        // for notifications.
        val inAppMessage = inAppMessages[0]
        Log.d(TAG, "getInAppNotification: ${inAppMessage.title}")
        EventUtils.pushEvent(
            EventUtils.EVENT_IN_APP_NOTIFICATION_RECEIVED,
            mapOf(EventUtils.EVENT_ATTRIBUTE_NOTIFICATION_ID to inAppMessage.id)
        )
        return inAppMessage
    }

    /**
     * Mark the given message as read.
     */
    fun markAsRead(messageId: String) {
        BRSharedPrefs.putReadInAppNotificationId(messageId)
    }
}
