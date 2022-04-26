/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.lifecycleScope
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.app.BreadApp
import com.breadwallet.logger.logDebug
import com.breadwallet.tools.animation.BRDialog
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.BrdUserState
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.tools.util.Utils
import com.breadwallet.ui.auth.AuthenticationController
import com.breadwallet.ui.disabled.DisabledController
import com.breadwallet.ui.keystore.KeyStoreController
import com.breadwallet.ui.login.LoginController
import com.breadwallet.ui.migrate.MigrateController
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.Navigator
import com.breadwallet.ui.navigation.OnCompleteAction
import com.breadwallet.ui.navigation.RouterNavigator
import com.breadwallet.ui.onboarding.IntroController
import com.breadwallet.ui.onboarding.OnBoardingController
import com.breadwallet.ui.pin.InputPinController
import com.breadwallet.ui.recovery.RecoveryKey
import com.breadwallet.ui.recovery.RecoveryKeyController
import com.breadwallet.ui.settings.SettingsController
import com.breadwallet.ui.settings.SettingsSection
import com.breadwallet.util.ControllerTrackingListener
import com.breadwallet.util.errorHandler
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.android.retainedSubDI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

// String extra containing a recovery phrase to bootstrap the recovery process. (debug only)
private const val EXTRA_RECOVER_PHRASE = "RECOVER_PHRASE"

/**
 * The main user entrypoint into the app.
 *
 * This activity serves as a Conductor router host and translates
 * platform events into Mobius events.
 */
@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity(), DIAware {

    companion object {
        const val EXTRA_DATA = "com.breadwallet.ui.MainActivity.EXTRA_DATA"
        const val EXTRA_PUSH_NOTIFICATION_CAMPAIGN_ID =
            "com.breadwallet.ui.MainActivity.EXTRA_PUSH_CAMPAIGN_ID"
    }

    override val di by retainedSubDI(closestDI()) {
        val router = router
        bind<Navigator>() with singleton {
            RouterNavigator { router }
        }
    }

    private val userManager by instance<BrdUserManager>()
    private val appScope by instance<CoroutineScope>()

    lateinit var router: Router
    private var trackingListener: ControllerTrackingListener? = null

    // NOTE: Used only to centralize deep link navigation handling.
    private val navigator by instance<Navigator>()

    private val resumedScope = CoroutineScope(
        Default + SupervisorJob() + errorHandler("resumedScope")
    )

    private var closeDebugDrawer = { false }
    private var launchedWithInvalidState = false

    @Suppress("ComplexMethod", "LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The view of this activity is nothing more than a Controller host with animation support
        val root = ChangeHandlerFrameLayout(this).also { view ->
            router = Conductor.attachRouter(this, view, savedInstanceState)
        }
        setContentView(if (BuildConfig.DEBUG) createDebugLayout(root, savedInstanceState) else root)

        trackingListener = ControllerTrackingListener(this).also(router::addChangeListener)

        appScope.launch(Main) {
            try {
                userManager.checkAccountInvalidated()
            } catch (e: UserNotAuthenticatedException) {
                finish()
                return@launch
            }
            val userState = userManager.getState()
            if (userState is BrdUserState.KeyStoreInvalid) {
                processUserState(userState)
                return@launch
            }

            userManager.lock()

            // Allow launching with a phrase to recover automatically
            val hasWallet = userManager.isInitialized()
            if (BuildConfig.DEBUG && intent.hasExtra(EXTRA_RECOVER_PHRASE) && !hasWallet) {
                val phrase = intent.getStringExtra(EXTRA_RECOVER_PHRASE)!!
                if (phrase.isNotBlank() && phrase.split(" ").size == RecoveryKey.M.RECOVERY_KEY_WORDS_COUNT) {
                    val controller = RecoveryKeyController(RecoveryKey.Mode.RECOVER, phrase)
                    router.setRoot(RouterTransaction.with(controller))
                    return@launch
                }
            }

            // The app is launched, no screen to be restored
            if (!router.hasRootController()) {
                val rootController = when {
                    userManager.isMigrationRequired() -> MigrateController()
                    else -> when (userManager.getState()) {
                        is BrdUserState.Disabled -> DisabledController()
                        is BrdUserState.Uninitialized -> IntroController()
                        else -> if (userManager.hasPinCode()) {
                            val intentUrl = processIntentData(intent)
                            LoginController(intentUrl)
                        } else {
                            InputPinController(OnCompleteAction.GO_HOME)
                        }
                    }
                }
                router.setRoot(
                    RouterTransaction.with(rootController)
                        .popChangeHandler(FadeChangeHandler())
                        .pushChangeHandler(FadeChangeHandler())
                )
            }
        }

        if (BuildConfig.DEBUG) {
            Utils.printPhoneSpecs()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDebugDrawer = { false }
        trackingListener?.run(router::removeChangeListener)
        trackingListener = null
        resumedScope.cancel()
    }

    override fun onResume() {
        super.onResume()
        BreadApp.setBreadContext(this)

        userManager.stateChanges()
            .dropWhile { router.backstackSize == 0 }
            .map { processUserState(it) }
            .flowOn(Main)
            .launchIn(resumedScope)
    }

    override fun onPause() {
        super.onPause()
        BreadApp.setBreadContext(null)
        resumedScope.coroutineContext.cancelChildren()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        router.onActivityResult(requestCode, resultCode, data)
        userManager.onActivityResult(requestCode, resultCode)
    }

    override fun onBackPressed() {
        // Defer to controller back-press control before exiting.
        if (!closeDebugDrawer() && !router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return checkOverlayAndDispatchTouchEvent(ev)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return

        val data = processIntentData(intent).orEmpty()
        if (data.isNotBlank() && userManager.isInitialized()) {
            val hasRoot = router.hasRootController()
            val isTopLogin = router.backstack.lastOrNull()?.controller is LoginController
            val isAuthenticated = !isTopLogin && hasRoot
            navigator.navigateTo(NavigationTarget.DeepLink(data, isAuthenticated))
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun createDebugLayout(root: View, bundle: Bundle?): View {
        val drawerDirection = Gravity.RIGHT
        val controller = SettingsController(SettingsSection.DEVELOPER_OPTION)
        return DrawerLayout(this).apply {
            lifecycleScope.launchWhenCreated {
                userManager.stateChanges().collect { state ->
                    val mode = if (state is BrdUserState.Enabled) {
                        LOCK_MODE_UNLOCKED
                    } else {
                        LOCK_MODE_LOCKED_CLOSED
                    }
                    setDrawerLockMode(mode, drawerDirection)
                }
            }
            closeDebugDrawer = {
                isDrawerOpen(drawerDirection).also {
                    closeDrawer(drawerDirection)
                }
            }
            addView(
                root,
                DrawerLayout.LayoutParams(
                    DrawerLayout.LayoutParams.MATCH_PARENT,
                    DrawerLayout.LayoutParams.MATCH_PARENT
                )
            )
            addView(
                NavigationView(context).apply {
                    addView(
                        ChangeHandlerFrameLayout(context).apply {
                            id = R.id.drawer_layout_id
                            Conductor.attachRouter(this@MainActivity, this, bundle)
                                .setRoot(RouterTransaction.with(controller))
                        },
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                },
                DrawerLayout.LayoutParams(
                    DrawerLayout.LayoutParams.WRAP_CONTENT,
                    DrawerLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = drawerDirection
                }
            )
        }
    }

    /** Process the new intent and return the url to browse if available */
    private fun processIntentData(intent: Intent): String? {
        if (intent.hasExtra(EXTRA_PUSH_NOTIFICATION_CAMPAIGN_ID)) {
            val campaignId = intent.getStringExtra(EXTRA_PUSH_NOTIFICATION_CAMPAIGN_ID)!!
            val attributes = mapOf(EventUtils.EVENT_ATTRIBUTE_CAMPAIGN_ID to campaignId)
            EventUtils.pushEvent(EventUtils.EVENT_MIXPANEL_APP_OPEN, attributes)
            EventUtils.pushEvent(EventUtils.EVENT_PUSH_NOTIFICATION_OPEN)
        }

        val data = intent.getStringExtra(EXTRA_DATA)
        return if (data.isNullOrBlank()) {
            intent.dataString
        } else data
    }

    private fun processUserState(userState: BrdUserState) {
        if (userState is BrdUserState.KeyStoreInvalid) {
            launchedWithInvalidState = true
            logDebug("Device state is invalid, $userState")
            val needsKeyStoreController = router.backstack
                .map(RouterTransaction::controller)
                .filterIsInstance<KeyStoreController>()
                .none()
            if (needsKeyStoreController) {
                router.setRoot(RouterTransaction.with(KeyStoreController()))
            }
        } else if (launchedWithInvalidState) {
            logDebug("Device state is now valid, recreating activity.")
            router.setBackstack(emptyList(), null)
            recreate()
        } else {
            if (userManager.isInitialized() && router.hasRootController()) {
                when (userState) {
                    BrdUserState.Locked -> lockApp()
                    is BrdUserState.Disabled -> disableApp()
                }
            }
        }
    }

    private fun isBackstackDisabled() = router.backstack
        .map(RouterTransaction::controller)
        .filterIsInstance<DisabledController>()
        .any()

    private fun isBackstackLocked() =
        router.backstack.lastOrNull()?.controller
            ?.let {
                // Backstack is locked or requires a pin
                it is LoginController || it is InputPinController ||
                    it is AuthenticationController ||
                    // Backstack is initialization flow
                    it is OnBoardingController || it is RecoveryKeyController ||
                    it is MigrateController
            } ?: false

    private fun disableApp() {
        if (isBackstackDisabled()) return

        logDebug("Disabling backstack.")
        router.pushController(
            RouterTransaction.with(DisabledController())
                .pushChangeHandler(FadeChangeHandler())
                .popChangeHandler(FadeChangeHandler())
        )
    }

    private fun lockApp() {
        if (isBackstackDisabled() || isBackstackLocked()) return

        val controller = when {
            userManager.hasPinCode() ->
                LoginController(showHome = router.backstackSize == 0)
            else -> InputPinController(
                onComplete = OnCompleteAction.GO_HOME,
                skipWriteDown = BRSharedPrefs.phraseWroteDown
            )
        }

        logDebug("Locking with controller=$controller")

        router.pushController(
            RouterTransaction.with(controller)
                .popChangeHandler(FadeChangeHandler())
                .pushChangeHandler(FadeChangeHandler())
        )
    }

    /**
     * Check if there is an overlay view over the screen, if an
     * overlay view is found the event won't be dispatched and
     * a dialog with a warning will be shown.
     *
     * @param event The touch screen event.
     * @return boolean Return true if this event was consumed or if an overlay view was found.
     */
    private fun checkOverlayAndDispatchTouchEvent(event: MotionEvent): Boolean {
        // Filter obscured touches by consuming them.
        if (event.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0) {
            if (event.action == MotionEvent.ACTION_UP) {
                BRDialog.showSimpleDialog(
                    this, getString(R.string.Android_screenAlteringTitle),
                    getString(R.string.Android_screenAlteringMessage)
                )
            }
            return true
        }
        return super.dispatchTouchEvent(event)
    }
}
