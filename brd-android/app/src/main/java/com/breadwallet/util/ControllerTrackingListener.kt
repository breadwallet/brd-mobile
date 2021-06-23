/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/31/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import android.app.Activity
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.google.firebase.analytics.FirebaseAnalytics

class ControllerTrackingListener(
    private val activity: Activity
) : ControllerChangeHandler.ControllerChangeListener {
    override fun onChangeCompleted(
        to: Controller?,
        from: Controller?,
        isPush: Boolean,
        container: ViewGroup,
        handler: ControllerChangeHandler
    ) {
        val screenName = to?.run { this::class.simpleName?.removeSuffix("Controller") }
        FirebaseAnalytics.getInstance(activity.applicationContext)
            .setCurrentScreen(activity, screenName, null)
    }

    override fun onChangeStarted(
        to: Controller?,
        from: Controller?,
        isPush: Boolean,
        container: ViewGroup,
        handler: ControllerChangeHandler
    ) = Unit
}
