/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 10/08/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail></mihail>@breadwallet.com> on 6/24/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.animation

import android.content.Context
import android.view.View
import android.view.animation.ScaleAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.breadwallet.appcore.R

object SpringAnimator {
    fun showExpandCameraGuide(view: View?) {
        if (view != null) {
            view.visibility = View.GONE
        }
        val trans = ScaleAnimation(
            0.0f, 1f, 0.0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        trans.duration = 800
        trans.interpolator = DecelerateOvershootInterpolator(1.5f, 2.5f)
        if (view != null) {
            view.visibility = View.VISIBLE
            view.startAnimation(trans)
        }
    }

    /**
     * Shows the springy animation on views
     */
    fun springView(view: View?) {
        if (view == null) return
        val trans = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        trans.duration = 1000
        trans.interpolator = DecelerateOvershootInterpolator(0.5f, 1f)
        view.visibility = View.VISIBLE
        view.startAnimation(trans)
    }

    /**
     * Shows the springy animation on views
     */
    fun shortSpringView(view: View?) {
        if (view == null) return
        val trans = ScaleAnimation(
            0.9f, 1f, 0.9f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        trans.duration = 200
        trans.interpolator = DecelerateOvershootInterpolator(1.3f, 1.4f)
        view.visibility = View.VISIBLE
        view.startAnimation(trans)
    }

    /**
     * Shows the springy bubble animation on views
     */
    fun showBubbleAnimation(view: View?) {
        if (view == null) return
        val trans = ScaleAnimation(
            0.75f, 1f, 0.75f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        trans.duration = 300
        trans.interpolator = DecelerateOvershootInterpolator(1.0f, 1.85f)
        view.visibility = View.VISIBLE
        view.startAnimation(trans)
    }

    fun failShakeAnimation(context: Context?, view: View?) {
        if (view == null) return
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        view.visibility = View.VISIBLE
        view.startAnimation(shake)
    }
}