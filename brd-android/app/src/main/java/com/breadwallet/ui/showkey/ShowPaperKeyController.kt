/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.showkey

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter
import com.breadwallet.R
import com.breadwallet.databinding.ControllerPaperKeyBinding
import com.breadwallet.databinding.FragmentWordItemBinding
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.navigation.OnCompleteAction
import com.breadwallet.ui.showkey.ShowPaperKey.E
import com.breadwallet.ui.showkey.ShowPaperKey.F
import com.breadwallet.ui.showkey.ShowPaperKey.M
import com.breadwallet.util.DefaultOnPageChangeListener
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ShowPaperKeyController(args: Bundle) : BaseMobiusController<M, E, F>(args) {

    companion object {
        private const val EXTRA_PHRASE = "phrase"
        private const val EXTRA_ON_COMPLETE = "on-complete"
        private const val NAVIGATION_BUTTONS_WEIGHT = 1
        private const val BUTTONS_LAYOUT_WEIGHT_SUM_DEFAULT = 2.0f
        private const val BUTTONS_LAYOUT_WEIGHT_SUM_SINGLE = 1.0f
    }

    constructor(
        phrase: List<String>,
        onComplete: OnCompleteAction? = null
    ) : this(
        bundleOf(
            EXTRA_PHRASE to phrase,
            EXTRA_ON_COMPLETE to onComplete?.name
        )
    )

    private val phrase: List<String> = arg(EXTRA_PHRASE)
    private val onComplete = argOptional<String>(EXTRA_ON_COMPLETE)
        ?.run(OnCompleteAction::valueOf)

    override val defaultModel = M.createDefault(phrase, onComplete, BRSharedPrefs.phraseWroteDown)
    override val update = ShowPaperKeyUpdate
    override val flowEffectHandler = subtypeEffectHandler<F, E> { }

    private val binding by viewBinding(ControllerPaperKeyBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            merge(
                nextButton.clicks().map { E.OnNextClicked },
                previousButton.clicks().map { E.OnPreviousClicked },
                closeButton.clicks().map { E.OnCloseClicked },
                callbackFlow<E> {
                    val channel = channel
                    val listener = object : DefaultOnPageChangeListener() {
                        override fun onPageSelected(position: Int) {
                            channel.offer(E.OnPageChanged(position))
                        }
                    }
                    wordsPager.addOnPageChangeListener(listener)
                    awaitClose {
                        wordsPager.removeOnPageChangeListener(listener)
                    }
                }
            )
        }
    }

    override fun M.render() {
        with(binding) {
            ifChanged(M::phrase) {
                wordsPager.adapter = WordPagerAdapter(this@ShowPaperKeyController, phrase)
            }
            ifChanged(M::currentWord) {
                wordsPager.currentItem = currentWord
                itemIndex.text = resources?.getString(
                    R.string.WritePaperPhrase_step,
                    currentWord + 1,
                    phrase.size
                )
                updateButtons(currentWord > 0)
            }
        }
    }

    /** Show or hide the "Previous" button used to navigate the ViewPager. */
    private fun updateButtons(showPrevious: Boolean) {
        with(binding) {
            val nextButtonParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            if (!showPrevious) {
                buttonsLayout.weightSum = BUTTONS_LAYOUT_WEIGHT_SUM_SINGLE

                nextButtonParams.weight = NAVIGATION_BUTTONS_WEIGHT.toFloat()
                nextButtonParams.gravity = Gravity.CENTER_HORIZONTAL
                nextButtonParams.setMargins(
                    resources!!.getDimension(R.dimen.margin).toInt(),
                    0,
                    resources!!.getDimension(R.dimen.margin).toInt(),
                    0
                )
                nextButton.layoutParams = nextButtonParams
                nextButton.height = resources!!.getDimension(R.dimen.large_button_height).toInt()

                previousButton.visibility = View.GONE
            } else {
                buttonsLayout.weightSum = BUTTONS_LAYOUT_WEIGHT_SUM_DEFAULT

                nextButtonParams.weight = NAVIGATION_BUTTONS_WEIGHT.toFloat()
                nextButtonParams.setMargins(0, 0, resources!!.getDimension(R.dimen.margin).toInt(), 0)
                nextButton.layoutParams = nextButtonParams
                nextButton.height = resources!!.getDimension(R.dimen.large_button_height).toInt()

                val previousButtonParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                previousButtonParams.weight = NAVIGATION_BUTTONS_WEIGHT.toFloat()
                previousButtonParams.setMargins(
                    resources!!.getDimension(R.dimen.margin).toInt(),
                    0,
                    0,
                    0
                )
                previousButton.layoutParams = previousButtonParams
                previousButton.visibility = View.VISIBLE
                previousButton.height = resources!!.getDimension(R.dimen.large_button_height).toInt()
            }
        }
    }
}

class WordPagerAdapter(
    host: Controller,
    private val words: List<String>
) : RouterPagerAdapter(host) {
    override fun configureRouter(router: Router, position: Int) {
        router.replaceTopController(RouterTransaction.with(WordController(words[position])))
    }

    override fun getCount() = words.size
}

class WordController(args: Bundle? = null) : BaseController(args) {
    companion object {
        private const val EXT_WORD = "word"
    }

    constructor(word: String) : this(
        bundleOf(EXT_WORD to word)
    )

    private val binding by viewBinding(FragmentWordItemBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.wordButton.text = arg(EXT_WORD)
    }
}
