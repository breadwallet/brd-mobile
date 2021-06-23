/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.breadwallet.databinding.ControllerIntroRecoverBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.recovery.RecoveryKeyController

class IntroRecoveryController(args: Bundle? = null) : BaseController(args) {

    private val binding by viewBinding(ControllerIntroRecoverBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.sendButton.setOnClickListener {
            router.pushController(
                RouterTransaction.with(RecoveryKeyController())
                    .popChangeHandler(HorizontalChangeHandler())
                    .pushChangeHandler(HorizontalChangeHandler())
            )
        }
    }
}
