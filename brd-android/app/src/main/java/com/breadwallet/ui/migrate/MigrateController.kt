/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.migrate

import android.content.Context
import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.breadwallet.app.BreadApp
import com.breadwallet.databinding.ControllerLoginBinding
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.keystore.KeyStoreController
import com.breadwallet.ui.login.LoginController
import com.breadwallet.ui.onboarding.IntroController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.direct
import org.kodein.di.erased.instance

class MigrateController(
    args: Bundle? = null
) : BaseController(args) {

    @Suppress("unused")
    private val binding by viewBinding(ControllerLoginBinding::inflate)

    private val userManager: BrdUserManager by instance()
    private val appScope: CoroutineScope by instance()

    private val mutex = Mutex()

    override fun onAttach(view: View) {
        super.onAttach(view)
        val context = applicationContext!!
        appScope.launch(Main) {
            mutex.withLock<Unit> {
                if (userManager.isMigrationRequired()) {
                    try {
                        migrateAccount(context)
                    } catch (e: UserNotAuthenticatedException) {
                        waitUntilAttached()
                        activity?.finish()
                    }
                } else if (isAttached) {
                    redirect()
                }
            }
        }
    }

    private fun redirect() {
        val target = if (userManager.isInitialized()) {
            LoginController()
        } else {
            IntroController()
        }
        router.replaceTopController(RouterTransaction.with(target))
    }

    private suspend fun migrateAccount(context: Context) {
        if (userManager.migrateKeystoreData()) {
            val breadApp = (context as BreadApp)
            // The one case where we need to invoke this outside of BreadApp, need to set the migrate flag
            breadApp.startWithInitializedWallet(direct.instance(), true)

            waitUntilAttached()
            router.replaceTopController(RouterTransaction.with(LoginController()))
        } else {
            waitUntilAttached()
            router.replaceTopController(RouterTransaction.with(KeyStoreController()))
        }
    }

    private suspend fun waitUntilAttached() {
        while (!isAttached) delay(1_000)
    }
}
