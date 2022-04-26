/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.breadwallet.databinding.ControllerIntroBinding
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.support.SupportController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val ANIMATION_MAX_DELAY = 15000L
private const val ANIMATION_DURATION = 3000L
private const val ICONS_TO_SHOW = 20

/**
 * Activity shown when there is no wallet, here the user can pick
 * between creating new wallet or recovering one with the paper key.
 */
class IntroController : BaseController() {

    private val binding by viewBinding(ControllerIntroBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            buttonNewWallet.setOnClickListener {
                EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_GET_STARTED)
                router.pushController(
                    RouterTransaction.with(OnBoardingController())
                        .popChangeHandler(HorizontalChangeHandler())
                        .pushChangeHandler(HorizontalChangeHandler())
                )
            }
            buttonRecoverWallet.setOnClickListener {
                EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_RESTORE_WALLET)
                router.pushController(
                    RouterTransaction.with(IntroRecoveryController())
                        .popChangeHandler(HorizontalChangeHandler())
                        .pushChangeHandler(HorizontalChangeHandler())
                )
            }
            faqButton.setOnClickListener {
                router.pushController(RouterTransaction.with(SupportController(slug = BRConstants.FAQ_START_VIEW)))
            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_APPEARED)
        startAnimations()
    }

    private fun startAnimations() {
        viewAttachScope.launch(Dispatchers.IO) {
            TokenUtil.waitUntilInitialized()
            val icons = TokenUtil.getTokenItems()
                .shuffled()
                .take(ICONS_TO_SHOW)
                .mapNotNull { token ->
                    TokenUtil.getTokenIconPath(token.symbol, false)
                }

            Main { loadViewsAndAnimate(icons) }
        }
    }

    private fun loadViewsAndAnimate(iconsPath: List<String>) {
        val context = router.activity
        val icons = mutableListOf<View>()

        with(binding) {
            for (path in iconsPath) {
                val icon = ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(100, 100)
                    id = View.generateViewId()
                    setImageURI(path.toUri())
                    alpha = 0.6f
                    visibility = View.INVISIBLE
                }
                icons.add(icon)
                introLayout.addView(icon)
            }

            val constraintSet = ConstraintSet()
            constraintSet.clone(introLayout)

            // Add constraints for the initial position
            for (icon in icons) {
                constraintSet.connect(
                    icon.id,
                    ConstraintSet.RIGHT,
                    introLayout.id,
                    ConstraintSet.RIGHT,
                    16
                )
                constraintSet.connect(
                    icon.id,
                    ConstraintSet.LEFT,
                    introLayout.id,
                    ConstraintSet.LEFT,
                    16
                )
                constraintSet.connect(
                    icon.id,
                    ConstraintSet.TOP,
                    logoView.id,
                    ConstraintSet.TOP,
                    16
                )
                constraintSet.connect(
                    icon.id,
                    ConstraintSet.BOTTOM,
                    logoView.id,
                    ConstraintSet.BOTTOM,
                    16
                )
            }
            constraintSet.applyTo(introLayout)

            val length = icons.size - 1
            for (i in 0..length) {
                animateIcon(icons[i])
            }
            viewAttachScope.launch(Main) {
                delay(ANIMATION_MAX_DELAY)
                loadViewsAndAnimate(iconsPath)
            }
        }
    }

    private fun animateIcon(icon: View) {
        viewAttachScope.launch(Main) {
            delay(Random.nextLong(0, ANIMATION_MAX_DELAY))
            icon.visibility = View.VISIBLE

            val angle = Random.nextDouble(0.0, 360.0) * PI / 180
            val hypotenuse = resources?.displayMetrics?.widthPixels?.toFloat() ?: 1000f
            val translationX = cos(angle) * hypotenuse
            val translationY = sin(angle) * hypotenuse
            val animX = ObjectAnimator.ofFloat(icon, "translationX", translationX.toFloat())
            val animY = ObjectAnimator.ofFloat(icon, "translationY", translationY.toFloat())
            val animAlpha = ObjectAnimator.ofFloat(icon, "alpha", 0.2f)
            AnimatorSet().apply {
                duration = ANIMATION_DURATION
                playTogether(animX, animY, animAlpha)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        if (isAttached) {
                            binding.introLayout.removeView(icon)
                        }
                    }

                    override fun onAnimationStart(animation: Animator?) = Unit
                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationCancel(animation: Animator?) = Unit
                })
                start()
            }
        }
    }
}
