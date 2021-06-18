/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/7/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.breadwallet.tools.security.BrdUserManager
import drewcarlson.blockset.BdbService

class AndroidBdbAuthProvider(private val userManager: BrdUserManager) : BdbService.AuthProvider {
    override fun readUserJwt(): String? = userManager.getBdbJwt()
}
