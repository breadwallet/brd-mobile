/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import com.bluelinelabs.conductor.Router

@Suppress("MagicNumber")
class SlideDetector(private val root: View) : View.OnTouchListener {

    constructor(router: Router, root: View) : this(root) {
        this.router = router
    }

    private var router: Router? = null

    private var origY: Float = 0f
    private var dY: Float = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                origY = root.y
                dY = root.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> if (event.rawY + dY > origY)
                root.animate()
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()
            MotionEvent.ACTION_UP -> if (root.y > origY + root.height / 5) {
                root.animate()
                    .y((root.height * 2).toFloat())
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(0.5f))
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            removeCurrentView()
                        }
                    })
                    .start()
            } else {
                root.animate()
                    .y(origY)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator(0.5f))
                    .start()
            }
            else -> return false
        }
        return true
    }

    private fun removeCurrentView() {
        router?.popCurrentController()
    }
}
