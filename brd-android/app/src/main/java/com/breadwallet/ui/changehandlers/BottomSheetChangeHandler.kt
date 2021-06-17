/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.changehandlers

import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler

/** Adds a transparent scrim to the previous view and slides the new view vertically. */
class BottomSheetChangeHandler : VerticalChangeHandler(ANIMATION_DURATION, false) {

    companion object {
        private const val ANIMATION_DURATION = 250L
    }

    override fun getAnimator(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean
    ) = animatorWithScrim(
        super.getAnimator(container, from, to, isPush, toAddedToContainer),
        from,
        to,
        isPush
    )
}

