/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 7/18/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet.spark.animation

import android.animation.Animator
import com.breadwallet.ui.wallet.spark.SparkView

/**
 * This interface is for animate SparkView when it changes
 *
 * Adapted from Robinhood's SparkView: https://github.com/robinhood/spark
 */
interface SparkAnimator {

    /**
     * Returns an Animator that performs the desired animation. Must call
     * [SparkView.setAnimationPath] for each animation frame.
     *
     * See [LineSparkAnimator] and [MorphSparkAnimator] for examples.
     *
     * @param sparkView The SparkView object
     */
    fun getAnimator(sparkView: SparkView): Animator?
}
