/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 7/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breadwallet.repository.NotificationsState
import com.breadwallet.repository.PushNotificationsSettingsRepositoryImpl
import com.breadwallet.tools.mvvm.Resource
import com.breadwallet.tools.threads.executor.BRExecutor

class NotificationSettingsViewModel : ViewModel() {

    val notificationsEnable = MutableLiveData<NotificationsState>()

    fun togglePushNotifications(enable: Boolean): LiveData<Resource<Void>> {
        val state = PushNotificationsSettingsRepositoryImpl.getNotificationsState()
        val resource = MutableLiveData<Resource<Void>>().apply { value = Resource.loading() }

        // Check if we need to update the settings.
        if ((state == NotificationsState.APP_ENABLED && !enable) ||
            (state == NotificationsState.APP_DISABLED && enable)
        ) {

            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
                val updated = PushNotificationsSettingsRepositoryImpl.togglePushNotifications(enable)
                if (updated) {
                    resource.postValue(Resource.success())
                } else {
                    resource.postValue(Resource.error())
                }
                notificationsEnable.postValue(PushNotificationsSettingsRepositoryImpl.getNotificationsState())
            }
        } else {
            resource.value = Resource.success()
        }
        return resource
    }

    /**
     * Check what is the current state of the notification settings. This is intended to be called
     * when we return to the settings activity to verify if the notifications are enabled on the
     * OS settings.
     */
    fun refreshState() {
        notificationsEnable.value = PushNotificationsSettingsRepositoryImpl.getNotificationsState()
    }
}
