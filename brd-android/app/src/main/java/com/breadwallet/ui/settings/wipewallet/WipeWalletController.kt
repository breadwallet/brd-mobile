/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/29/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.wipewallet

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.breadwallet.databinding.ControllerWipeWalletBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.recovery.RecoveryKey
import com.breadwallet.ui.recovery.RecoveryKeyController

class WipeWalletController : BaseController() {

    private val binding by viewBinding(ControllerWipeWalletBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.continueBtn.setOnClickListener {
            router.pushController(
                RouterTransaction.with(RecoveryKeyController(RecoveryKey.Mode.WIPE))
                    .pushChangeHandler(HorizontalChangeHandler())
                    .popChangeHandler(HorizontalChangeHandler())
            )
        }
        binding.closeBtn.setOnClickListener { router.popCurrentController() }
    }
}
