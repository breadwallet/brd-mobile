/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 6/7/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.notification

import androidx.lifecycle.ViewModel
import com.breadwallet.model.InAppMessage
import com.breadwallet.repository.MessagesRepository
import com.breadwallet.tools.threads.executor.BRExecutor
import com.breadwallet.tools.util.EventUtils

class InAppNotificationViewModel(val notification: InAppMessage) : ViewModel() {

    /**
     * Getter of a map with the id and the message_id to be used for analytics.
     */
    private val idsAsAttributes: Map<String, String>
        get() = mapOf(
            EventUtils.EVENT_ATTRIBUTE_NOTIFICATION_ID to notification.id,
            EventUtils.EVENT_ATTRIBUTE_MESSAGE_ID to notification.messageId
        )

    /**
     * Mark message as read, this is done when the message is dismissed or the action button is
     * clicked.
     */
    fun markAsRead(actionButtonClicked: Boolean) {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            MessagesRepository.markAsRead(notification.messageId)

            if (actionButtonClicked) {
                EventUtils.pushEvent(
                    EventUtils.EVENT_IN_APP_NOTIFICATION_CTA_BUTTON,
                    idsAsAttributes.toMutableMap().apply {
                        put(EventUtils.EVENT_ATTRIBUTE_NOTIFICATION_CTA_URL, notification.actionButtonUrl.orEmpty())
                    }
                )
            } else {
                EventUtils.pushEvent(EventUtils.EVENT_IN_APP_NOTIFICATION_DISMISSED, idsAsAttributes)
            }
        }
    }

    /**
     * Mark the message as shown.
     */
    fun markAsShown() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            EventUtils.pushEvent(EventUtils.EVENT_IN_APP_NOTIFICATION_APPEARED, idsAsAttributes)
        }
    }
}
