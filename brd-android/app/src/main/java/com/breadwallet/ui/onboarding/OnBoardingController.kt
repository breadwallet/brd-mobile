/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter
import com.breadwallet.R
import com.breadwallet.databinding.ControllerOnBoardingBinding
import com.breadwallet.databinding.ControllerOnboardingPageBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.onboarding.OnBoarding.E
import com.breadwallet.ui.onboarding.OnBoarding.F
import com.breadwallet.ui.onboarding.OnBoarding.M
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.kodein.di.direct
import org.kodein.di.instance

class OnBoardingController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args) {

    private val activeIndicator by lazy {
        ContextCompat.getDrawable(applicationContext!!, R.drawable.page_indicator_active)
    }
    private val inactiveIndicator by lazy {
        ContextCompat.getDrawable(applicationContext!!, R.drawable.page_indicator_inactive)
    }

    override val defaultModel = M.DEFAULT
    override val init = OnBoardingInit
    override val update = OnBoardingUpdate

    override val flowEffectHandler
        get() = createOnBoardingHandler(direct.instance())

    private val binding by viewBinding(ControllerOnBoardingBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.viewPager.adapter = OnBoardingPageAdapter()
    }

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            merge(
                buttonSkip.clicks().map { E.OnSkipClicked },
                buttonBack.clicks().map { E.OnBackClicked },
                callbackFlow<E.OnPageChanged> {
                    val channel = channel
                    val listener = object : ViewPager.SimpleOnPageChangeListener() {
                        override fun onPageSelected(position: Int) {
                            channel.offer(E.OnPageChanged(position + 1))
                        }
                    }
                    viewPager.addOnPageChangeListener(listener)
                    awaitClose {
                        viewPager.removeOnPageChangeListener(listener)
                    }
                }
            )
        }
    }

    override fun M.render() {
        with(binding) {
            ifChanged(M::page) { page ->
                listOf(indicator1, indicator2, indicator3)
                    .forEachIndexed { index, indicator ->
                        indicator.background = when (page) {
                            index + 1 -> activeIndicator
                            else -> inactiveIndicator
                        }
                    }
            }

            ifChanged(M::isFirstPage) { isFirstPage ->
                buttonSkip.isVisible = isFirstPage
                buttonBack.isVisible = isFirstPage
            }

            ifChanged(M::isLoading) { isLoading ->
                loadingView.root.isVisible = isLoading
                buttonSkip.isEnabled = !isLoading
            }
        }
    }

    inner class OnBoardingPageAdapter : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val root = when (position) {
                    0 -> PageOneController()
                    1 -> PageTwoController()
                    2 -> PageThreeController()
                    else -> error("Unknown position")
                }
                router.setRoot(RouterTransaction.with(root))
            }
        }

        override fun getCount(): Int = 3
    }

    override fun handleBack() = currentModel.isLoading
}

class PageOneController(args: Bundle? = null) : BaseController(args) {
    private val binding by viewBinding(ControllerOnboardingPageBinding::inflate)
    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.primaryText.setText(R.string.OnboardingPageTwo_title)
        binding.secondaryText.setText(R.string.OnboardingPageTwo_subtitle)
    }
}

class PageTwoController(args: Bundle? = null) : BaseController(args) {
    private val binding by viewBinding(ControllerOnboardingPageBinding::inflate)
    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.primaryText.setText(R.string.OnboardingPageThree_title)
        binding.secondaryText.setText(R.string.OnboardingPageThree_subtitle)
        binding.imageView.setImageResource(R.drawable.ic_currencies)
    }
}

class PageThreeController(args: Bundle? = null) : BaseController(args) {
    private val binding by viewBinding(ControllerOnboardingPageBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        val onBoardingController = (parentController as OnBoardingController)

        with(binding) {
            lastScreenTitle.isVisible = true
            buttonBuy.isVisible = true
            buttonBrowse.isVisible = true
            primaryText.isVisible = false
            secondaryText.isVisible = false
            imageView.isVisible = false
            buttonBuy.setOnClickListener {
                onBoardingController.eventConsumer.accept(E.OnBuyClicked)
            }
            buttonBrowse.setOnClickListener {
                onBoardingController.eventConsumer.accept(E.OnBrowseClicked)
            }
        }
    }
}
