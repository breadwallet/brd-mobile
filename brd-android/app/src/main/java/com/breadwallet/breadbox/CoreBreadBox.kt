/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.blockset.walletkit.Account
import com.blockset.walletkit.Key
import com.blockset.walletkit.Network
import com.blockset.walletkit.System
import com.blockset.walletkit.SystemClient
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.Wallet
import com.breadwallet.app.BreadApp
import com.breadwallet.ext.throttleLatest
import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logInfo
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.util.Bip39Reader
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.util.errorHandler
import com.platform.interfaces.WalletProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val DEFAULT_THROTTLE_MS = 500L
private const val AGGRESSIVE_THROTTLE_MS = 800L

@Suppress("TooManyFunctions")
internal class CoreBreadBox(
    private val storageFile: File,
    override val isMainnet: Boolean = false,
    private val walletProvider: WalletProvider,
    private val blockchainDb: SystemClient,
    private val userManager: BrdUserManager
) : BreadBox {

    companion object {
        fun setWords() {
            // Set default words list
            val context = BreadApp.getBreadContext()
            val words = Bip39Reader.getBip39Words(context, BRSharedPrefs.recoveryKeyLanguage)
            Key.setDefaultWordList(words)
        }
    }

    @Volatile
    private var system: System? = null
    private val systemExecutor = Executors.newSingleThreadScheduledExecutor()

    private var openScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + errorHandler("openScope")
    )

    private var networkManager: NetworkManager? = null
    private val systemListener = SystemListenerImpl()
    private val _isOpen = AtomicBoolean(false)
    override var isOpen: Boolean
        get() = _isOpen.get()
        set(value) {
            _isOpen.set(value)
        }

    @Synchronized
    override fun open(account: Account) {
        logDebug("Opening CoreBreadBox")

        check(!isOpen) { "open() called while BreadBox was open." }
        check(account.serialize().isNotEmpty()) { "Account.serialize() contains 0 bytes" }

        if (!storageFile.exists()) {
            logDebug("Making storage directories")
            check(storageFile.mkdirs()) {
                "Failed to create storage directory: ${storageFile.absolutePath}"
            }
        }

        fun newSystem() = System.create(
            systemExecutor,
            systemListener,
            account,
            isMainnet,
            storageFile.absolutePath,
            blockchainDb
        ).apply {
            logDebug("Created new System instance")
            configure()
        }

        system = (system ?: newSystem()).also { system ->
            logDebug("Dispatching initial System values")

            system.resume()

            networkManager = NetworkManager(
                system,
                openScope + systemExecutor.asCoroutineDispatcher(),
                listOf(DefaultNetworkInitializer(userManager))
            ).also {
                systemListener.init(it, system)
            }
        }

        isOpen = true

        walletProvider
            .enabledWallets()
            .onEach { enabledWallets ->
                networkManager?.enabledWallets = enabledWallets
                systemListener.updateWallets(system?.wallets)
            }
            .launchIn(openScope)

        walletProvider
            .walletModes()
            .onEach { modes ->
                networkManager?.managerModes = modes
                systemListener.updateWallets(system?.wallets)
            }
            .launchIn(openScope)

        logInfo("BreadBox opened successfully")
    }

    @Synchronized
    override fun close(wipe: Boolean) {
        logDebug("Closing BreadBox")

        check(isOpen) { "BreadBox must be opened before calling close()." }

        openScope.cancel()
        openScope = CoroutineScope(
            SupervisorJob() + Dispatchers.Default + errorHandler("openScope")
        )

        checkNotNull(system).pause()

        if (wipe) {
            System.wipe(system)
            system = null
        }

        isOpen = false

        networkManager = null
    }

    override fun system(): Flow<System> =
        systemListener.systemEvents
            .dropWhile { !isOpen }
            .mapNotNull { system }

    override fun account(): Flow<Account> =
        systemListener.account.filterNotNull()

    override fun wallets(filterByTracked: Boolean): Flow<List<Wallet>> =
        combine(
            systemListener.walletEvents,
            systemListener.walletManagerEvents,
            systemListener.wallets
        ) { _, _, wallets -> wallets }
            .throttleLatest(AGGRESSIVE_THROTTLE_MS)
            .filterNotNull()
            .mapNotNull { wallets ->
                when {
                    filterByTracked -> {
                        wallets.filterByCurrencyIds(
                            walletProvider.enabledWallets().first()
                        )
                    }
                    else -> wallets
                }
            }

    override fun wallet(currencyCode: String): Flow<Wallet> =
        combine(
            systemListener.walletEvents,
            systemListener.walletManagerEvents,
            systemListener.wallets
        ) { _, _, wallets -> wallets }
            .throttleLatest(DEFAULT_THROTTLE_MS)
            .filterNotNull()
            .run {
                if (currencyCode.contains(":")) {
                    mapNotNull { wallet ->
                        wallet.firstOrNull {
                            it.currency.uids.equals(currencyCode, true)
                        }
                    }
                } else {
                    mapNotNull { wallet ->
                        wallet.firstOrNull {
                            it.currency.code.equals(currencyCode, true)
                        }
                    }
                }
            }

    override fun currencyCodes(): Flow<List<String>> =
        combine(
            walletProvider.enabledWallets().throttleLatest(AGGRESSIVE_THROTTLE_MS),
            wallets()
        ) { enabledWallets, wallets ->
            enabledWallets
                .associateWith { wallets.findByCurrencyId(it) }
                .mapValues { (currencyId, wallet) ->
                    wallet?.currency?.code ?: TokenUtil.tokenForCurrencyId(currencyId)
                        ?.symbol?.toLowerCase(Locale.ROOT)
                }.values
                .filterNotNull()
                .toList()
        }.throttleLatest(AGGRESSIVE_THROTTLE_MS)
            .distinctUntilChanged()

    override fun walletTransfers(currencyCode: String): Flow<List<Transfer>> =
        combine(
            systemListener.transferEvents,
            systemListener.walletEvents,
            systemListener.transfers.mapNotNull { walletMap ->
                walletMap.keys
                    .find { currencyCode.equals(it.currency.code, true) }
                    ?.run(walletMap::get)
            }
        ) { _, _, walletTransfers -> walletTransfers }
            .throttleLatest(AGGRESSIVE_THROTTLE_MS)

    override fun walletTransfer(currencyCode: String, transferHash: String): Flow<Transfer> {
        return combine(
            systemListener.transferEvents,
            systemListener.walletEvents,
            systemListener.transfers.mapNotNull { walletMap ->
                walletMap.keys
                    .find { currencyCode.equals(it.currency.code, true) }
                    ?.run(walletMap::get)
            }
        ) { _, _, transfers -> transfers }
            .mapNotNull { transfers ->
                transfers.find { transfer ->
                    transfer.wallet.currency.code.equals(currencyCode, true) &&
                        transfer.hash.isPresent &&
                        transfer.hashString().equals(transferHash, true)
                }
            }
    }

    override fun walletTransfer(currencyCode: String, transfer: Transfer): Flow<Transfer> {
        return combine(
            systemListener.transfer,
            systemListener.walletEvents,
            systemListener.transfers.mapNotNull { walletsMap ->
                walletsMap.keys
                    .find { it.currency.code.equals(currencyCode, true) }
                    ?.run(walletsMap::get)
            }
        ) { _, _, transfers -> transfers }
            .mapNotNull { transfers ->
                transfers.find { updatedTransfer ->
                    (transfer == updatedTransfer || (transfer.hash.isPresent && transfer.hash == updatedTransfer.hash))
                }
            }
    }

    override fun initializeWallet(currencyCode: String) {
        check(isOpen) { "initializeWallet cannot be called before open." }
        val system = checkNotNull(system)
        val networkManager = checkNotNull(networkManager)
        val network = system.networks.find { it.containsCurrencyCode(currencyCode) }
        checkNotNull(network) {
            "Network with currency code '$currencyCode' not found."
        }
        openScope.launch {
            networkManager.completeNetworkInitialization(network.currency.uids)
        }
    }

    override fun walletState(currencyCode: String): Flow<WalletState> =
        system()
            .map { system -> system.networks.find { it.containsCurrencyCode(currencyCode) } }
            .mapNotNull { network ->
                network?.currency?.uids ?: TokenUtil.tokenForCode(currencyCode)?.currencyId
            }
            .take(1)
            .flatMapLatest { uids ->
                checkNotNull(networkManager).networkState(uids).map { networkState ->
                    when (networkState) {
                        is NetworkState.Initialized -> WalletState.Initialized
                        is NetworkState.Loading -> WalletState.Loading
                        is NetworkState.ActionNeeded -> WalletState.WaitingOnAction
                        is NetworkState.Error -> WalletState.Error
                    }
                }
            }

    override fun networks(whenDiscoveryComplete: Boolean): Flow<List<Network>> =
        system().transform {
            if (whenDiscoveryComplete) {
                if (systemListener.discovery.value) {
                    emit(it.networks)
                }
            } else {
                emit(it.networks)
            }
        }

    override fun getSystemUnsafe(): System? = system
}
