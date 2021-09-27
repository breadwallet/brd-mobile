/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.app

import android.annotation.*
import android.app.*
import android.content.*
import android.net.*
import android.os.*
import androidx.annotation.*
import androidx.camera.camera2.*
import androidx.camera.core.*
import androidx.core.content.*
import androidx.lifecycle.*
import androidx.security.crypto.*
import com.blockset.walletkit.Api
import com.blockset.walletkit.brd.ApiProvider
import com.brd.addressresolver.*
import com.brd.api.*
import com.brd.bakerapi.*
import com.brd.exchange.*
import com.brd.prefs.*
import com.breadwallet.*
import com.breadwallet.BuildConfig
import com.breadwallet.breadbox.*
import com.breadwallet.di.getAppModule
import com.breadwallet.logger.*
import com.breadwallet.platform.interfaces.*
import com.breadwallet.repository.*
import com.breadwallet.tools.crypto.*
import com.breadwallet.tools.manager.*
import com.breadwallet.tools.security.*
import com.breadwallet.tools.services.*
import com.breadwallet.tools.util.*
import com.breadwallet.ui.uigift.*
import com.breadwallet.util.*
import com.breadwallet.util.usermetrics.*
import com.platform.*
import com.platform.interfaces.*
import com.platform.sqlite.*
import com.platform.tools.*
import drewcarlson.blockset.*
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.DirectDI
import org.kodein.di.android.x.androidXModule
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.*
import java.util.*
import java.util.regex.*

private const val LOCK_TIMEOUT = 180_000L // 3 minutes in milliseconds

const val WALLETKIT_DATA_DIR_NAME = "cryptocore"

@Suppress("TooManyFunctions")
class BreadApp : Application(), DIAware, CameraXConfig.Provider {

    companion object {
        init {
            Api.initialize(ApiProvider.getInstance())
        }

        // The wallet ID is in the form "xxxx xxxx xxxx xxxx" where x is a lowercase letter or a number.
        private const val WALLET_ID_PATTERN = "^[a-z0-9 ]*$"
        private const val WALLET_ID_SEPARATOR = " "
        private const val NUMBER_OF_BYTES_FOR_SHA256_NEEDED = 10

        @SuppressLint("StaticFieldLeak")
        private lateinit var mInstance: BreadApp

        @SuppressLint("StaticFieldLeak")
        private var mCurrentActivity: Activity? = null

        // TODO: Find better place/means for this
        fun getDefaultEnabledWallets() = when {
            BuildConfig.BITCOIN_TESTNET -> listOf(
                "bitcoin-testnet:__native__",
                "ethereum-ropsten:__native__",
                "ethereum-ropsten:0x558ec3152e2eb2174905cd19aea4e34a23de9ad6"
            )
            else -> listOf(
                "bitcoin-mainnet:__native__",
                "ethereum-mainnet:__native__",
                "ethereum-mainnet:0x558ec3152e2eb2174905cd19aea4e34a23de9ad6",
                "ethereum-mainnet:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
            )
        }

        /**
         * Initialize the wallet id (rewards id), and save it in the SharedPreferences.
         */
        private fun initializeWalletId(appScope: CoroutineScope, breadBox: BreadBox) {
            appScope.launch(Dispatchers.Main) {
                val walletId = breadBox
                    .wallets(false)
                    .mapNotNull { wallets ->
                        wallets.find { it.currency.code.isEthereum() }
                    }
                    .take(1)
                    .map { generateWalletId(it.target.toString()) }
                    .flowOn(Dispatchers.Default)
                    .first()
                if (walletId.isNullOrBlank() || !walletId.matches(WALLET_ID_PATTERN.toRegex())) {
                    val error = IllegalStateException("Generated corrupt walletId: $walletId")
                    BRReportsManager.reportBug(error)
                }
                BRSharedPrefs.putWalletRewardId(id = walletId ?: "")
            }
        }

        /**
         * Generates the wallet id (rewards id) based on the Ethereum address. The format of the id is
         * "xxxx xxxx xxxx xxxx", where x is a lowercase letter or a number.
         *
         * @return The wallet id.
         */
        // TODO: This entire operation should be moved into a separate class.
        @Synchronized
        @Suppress("ReturnCount")
        fun generateWalletId(address: String): String? {
            try {
                // Remove the first 2 characters i.e. 0x
                val rawAddress = address.drop(2)

                // Get the address bytes.
                val addressBytes = rawAddress.toByteArray()

                // Run SHA256 on the address bytes.
                val sha256Address = CryptoHelper.sha256(addressBytes) ?: byteArrayOf()
                if (sha256Address.isEmpty()) {
                    BRReportsManager.reportBug(IllegalAccessException("Failed to generate SHA256 hash."))
                    return null
                }

                // Get the first 10 bytes of the SHA256 hash.
                val firstTenBytes =
                    sha256Address.sliceArray(0 until NUMBER_OF_BYTES_FOR_SHA256_NEEDED)

                // Convert the first 10 bytes to a lower case string.
                val base32String = Base32.encode(firstTenBytes).toLowerCase(Locale.ROOT)

                // Insert a space every 4 chars to match the specified format.
                val builder = StringBuilder()
                val matcher = Pattern.compile(".{1,4}").matcher(base32String)
                var separator = ""
                while (matcher.find()) {
                    val piece = base32String.substring(matcher.start(), matcher.end())
                    builder.append(separator + piece)
                    separator = WALLET_ID_SEPARATOR
                }
                return builder.toString()
            } catch (e: UnsupportedEncodingException) {
                logError("Unable to get address bytes.", e)
                return null
            }
        }

        // TODO: Refactor so this does not store the current activity like this.
        @JvmStatic
        @Deprecated("")
        fun getBreadContext(): Context {
            var app: Context? = mCurrentActivity
            if (app == null) {
                app = mInstance
            }
            return app
        }

        // TODO: Refactor so this does not store the current activity like this.
        @JvmStatic
        fun setBreadContext(app: Activity?) {
            mCurrentActivity = app
        }

        /** Provides access to [DirectDI]. Meant only for Java compatibility. **/
        @JvmStatic
        fun getKodeinInstance(): DirectDI {
            return mInstance.direct
        }
    }

    private val startedScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + errorHandler("startedScope")
    )

    override val di by DI.lazy {
        importOnce(androidXModule(this@BreadApp))
        importOnce(getAppModule(this@BreadApp))
    }

    private var accountLockJob: Job? = null

    /** [CoroutineScope] matching the lifetime of the application. */
    private val applicationScope by instance<CoroutineScope>()

    private val brdApi by instance<BrdApiClient>()
    private val apiClient by instance<APIClient>()
    private val giftTracker by instance<GiftTracker>()
    private val userManager by instance<BrdUserManager>()
    private val ratesFetcher by instance<RatesFetcher>()
    private val accountMetaData by instance<AccountMetaDataProvider>()
    private val conversionTracker by instance<ConversionTracker>()
    private val connectivityStateProvider by instance<ConnectivityStateProvider>()
    private val brdPreferences by instance<BrdPreferences>()
    private val breadBox by instance<BreadBox>()

    override fun onCreate() {
        super.onCreate()
        installHooks()
        mInstance = this

        BRKeyStore.provideContext(this)
        BRClipboardManager.provideContext(this)
        BRSharedPrefs.initialize(this, applicationScope)
        TokenHolder.provideContext(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationLifecycleObserver())
        ApplicationLifecycleObserver.addApplicationLifecycleListener { event ->
            logDebug(event.name)
            when (event) {
                Lifecycle.Event.ON_START -> handleOnStart()
                Lifecycle.Event.ON_STOP -> handleOnStop()
                Lifecycle.Event.ON_DESTROY -> handleOnDestroy()
                else -> Unit
            }
        }

        CoreBreadBox.setWords()
        applicationScope.launch {
            ServerBundlesHelper.extractBundlesIfNeeded(mInstance)
            TokenUtil.initialize(mInstance, false, !BuildConfig.BITCOIN_TESTNET)
        }

        if (brdPreferences.hydraActivated) {
            direct.instance<ExchangeDataLoader>().fetchData()
        } else {
            // Start our local server as soon as the application instance is created, since we need to
            // display support WebViews during onboarding.
            HTTPServer.getInstance().startServer(this)
        }
    }

    /**
     * Each time the app resumes, check to see if the device state is valid.
     * Even if the wallet is not initialized, we may need tell the user to enable the password.
     */
    private fun handleOnStart() {
        accountLockJob?.cancel()
        BreadBoxCloseWorker.cancelEnqueuedWork()
        userManager
            .stateChanges()
            .distinctUntilChanged()
            .filterIsInstance<BrdUserState.Enabled>()
            .onEach {
                if (!userManager.isMigrationRequired()) {
                    startWithInitializedWallet(breadBox)
                }
            }
            .launchIn(startedScope)
    }

    private fun handleOnStop() {
        if (userManager.isInitialized()) {
            accountLockJob = applicationScope.launch {
                delay(LOCK_TIMEOUT)
                userManager.lock()
            }
            BreadBoxCloseWorker.enqueueWork()
            applicationScope.launch {
                EventUtils.saveEvents(this@BreadApp)
                EventUtils.pushToServer(this@BreadApp)
            }
        }
        logDebug("Shutting down HTTPServer.")
        HTTPServer.getInstance().stopServer()

        startedScope.coroutineContext.cancelChildren()
    }

    private fun handleOnDestroy() {
        if (HTTPServer.getInstance().isRunning) {
            logDebug("Shutting down HTTPServer.")
            HTTPServer.getInstance().stopServer()
        }

        connectivityStateProvider.close()
        breadBox.apply { if (isOpen) close() }
        applicationScope.cancel()
    }

    fun startWithInitializedWallet(breadBox: BreadBox, migrate: Boolean = false) {
        val context = mInstance.applicationContext
        incrementAppForegroundedCounter()

        if (!breadBox.isOpen) {
            val account = checkNotNull(userManager.getAccount()) {
                "Wallet is initialized but Account is null"
            }

            breadBox.open(account)
        }

        initializeWalletId(applicationScope, breadBox)

        applicationScope.launch {
            preflight()

            BRDFirebaseMessagingService.initialize(context)
            if (!brdPreferences.hydraActivated) {
                HTTPServer.getInstance().startServer(this@BreadApp)
                apiClient.updatePlatform(this)
            }
            launch { UserMetricsUtil.makeUserMetricsRequest(context) }
            startedScope.launch {
                accountMetaData.recoverAll(migrate)
                giftTracker.checkUnclaimedGifts()
            }
            conversionTracker.start(startedScope)
        }

        ratesFetcher.start(startedScope)
    }

    private suspend fun preflight() {
        if (!brdPreferences.hydraActivated) {
            val preflight = brdApi.preflight()
            if (preflight?.activate == true) {
                brdPreferences.hydraActivated = true
                userManager.removeToken()
                brdApi.host = BrdApiHost.hostFor(BuildConfig.DEBUG, true)
            }
        }
        if (brdPreferences.hydraActivated && !brdPreferences.isRewardsAddressSet) {
            brdPreferences.isRewardsAddressSet =
                brdApi.setMe(breadBox.wallet(eth).first().target.toString())
        }
    }

    private fun incrementAppForegroundedCounter() {
        BRSharedPrefs.putInt(
            BRSharedPrefs.APP_FOREGROUNDED_COUNT,
            BRSharedPrefs.getInt(BRSharedPrefs.APP_FOREGROUNDED_COUNT, 0) + 1
        )
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    @VisibleForTesting
    fun clearApplicationData() {
        try {
            startedScope.coroutineContext.cancelChildren()
            applicationScope.coroutineContext.cancelChildren()
            val breadBox = direct.instance<BreadBox>()
            if (breadBox.isOpen) {
                breadBox.close()
            }
            (userManager as CryptoUserManager).wipeAccount()

            File(filesDir, WALLETKIT_DATA_DIR_NAME).deleteRecursively()

            PlatformSqliteHelper.getInstance(this)
                .writableDatabase
                .delete(PlatformSqliteHelper.KV_STORE_TABLE_NAME, null, null)

            getSharedPreferences(BRSharedPrefs.PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
        } catch (e: Throwable) {
            logError("Failed to clear application data", e)
        }
    }

    fun createHttpHeaders(): Map<String, String> {
        // Split the default device user agent string by spaces and take the first string.
        // Example user agent string: "Dalvik/1.6.0 (Linux; U;Android 5.1; LG-F320SBuild/KOT49I.F320S22g) Android/9"
        // We only want: "Dalvik/1.6.0"
        val deviceUserAgent =
            (System.getProperty(APIClient.SYSTEM_PROPERTY_USER_AGENT) ?: "")
                .split("\\s".toRegex())
                .firstOrNull()

        // The BRD server expects the following user agent: appName/appVersion engine/engineVersion plaform/plaformVersion
        val brdUserAgent =
            "${APIClient.UA_APP_NAME}${BuildConfig.VERSION_CODE} $deviceUserAgent ${APIClient.UA_PLATFORM}${Build.VERSION.RELEASE}"

        return mapOf(
            APIClient.HEADER_IS_INTERNAL to if (BuildConfig.IS_INTERNAL_BUILD) BRConstants.TRUE else BRConstants.FALSE,
            APIClient.HEADER_TESTFLIGHT to if (BuildConfig.DEBUG) BRConstants.TRUE else BRConstants.FALSE,
            APIClient.HEADER_TESTNET to if (BuildConfig.BITCOIN_TESTNET) BRConstants.TRUE else BRConstants.FALSE,
            APIClient.HEADER_USER_AGENT to brdUserAgent
        )
    }
}
