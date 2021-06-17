/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 6/18/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.breadwallet.R
import com.breadwallet.tools.manager.BRSharedPrefs

/**
 * Responsible of handling the rules for showing the dialog to request to user to submit
 */
object AppReviewPromptManager {

    private const val GOOGLE_PLAY_APP_URI = "market://details?id=com.breadwallet"
    private const val GOOGLE_PLAY_URI =
        "https://play.google.com/store/apps/details?id=com.breadwallet"
    private const val GOOGLE_PLAY_PACKAGE = "com.android.vending"

    /**
     * Open Google Play from [activity] for the user to submit a review of the app.
     */
    fun openGooglePlay(activity: Activity) {
        BRSharedPrefs.appRatePromptHasRated = true
        // Try to send an intent to google play and if that fails open google play in the browser.
        try {
            val googlePlayIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_APP_URI))
                .setPackage(GOOGLE_PLAY_PACKAGE)
            activity.startActivity(googlePlayIntent)
        } catch (exception: android.content.ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_URI)))
        }
        activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    fun shouldPrompt(): Boolean =
        BRSharedPrefs.appRatePromptShouldPrompt && !BRSharedPrefs.appRatePromptDontAskAgain && !BRSharedPrefs.appRatePromptHasRated

    fun shouldTrackConversions(): Boolean =
        !BRSharedPrefs.appRatePromptHasRated && !BRSharedPrefs.appRatePromptDontAskAgain

    fun dismissPrompt() {
        BRSharedPrefs.appRatePromptShouldPrompt = false
        BRSharedPrefs.appRatePromptShouldPromptDebug = false
    }

    fun neverAskAgain() {
        BRSharedPrefs.appRatePromptDontAskAgain = true
    }
}
