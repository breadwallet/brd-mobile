/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/17/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.analytics

import android.os.Bundle
import android.view.View
import com.breadwallet.databinding.ControllerShareDataBinding
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.ui.BaseController

class ShareDataController(args: Bundle? = null) : BaseController(args) {

    private val binding by viewBinding(ControllerShareDataBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.toggleButton.isChecked = BRSharedPrefs.getShareData()
        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            BRSharedPrefs.putShareData(isChecked)
        }

        binding.backButton.setOnClickListener {
            router.popCurrentController()
        }
    }
}
