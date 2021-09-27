/**
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 9/3/2021.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.blockset.walletkit.SystemClient
import com.blockset.walletkit.brd.systemclient.BlocksetSystemClient
import com.brd.addressresolver.AddressResolver
import com.brd.api.AndroidBdbAuthProvider
import com.brd.api.AndroidBrdAuthProvider
import com.brd.api.BrdApiClient
import com.brd.api.BrdApiHost
import com.brd.bakerapi.BakersApiClient
import com.brd.exchange.ExchangeDataLoader
import com.brd.prefs.AndroidPreferences
import com.brd.prefs.BrdPreferences
import com.brd.prefs.Preferences
import com.breadwallet.BuildConfig
import com.breadwallet.app.BreadApp
import com.breadwallet.app.ConversionTracker
import com.breadwallet.app.GiftTracker
import com.breadwallet.app.WALLETKIT_DATA_DIR_NAME
import com.breadwallet.breadbox.BdbAuthInterceptor
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.CoreBreadBox
import com.breadwallet.getFlipperOkhttpInterceptor
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.repository.ExperimentsRepository
import com.breadwallet.repository.ExperimentsRepositoryImpl
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.manager.BRReportsManager
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.manager.ConnectivityStateProvider
import com.breadwallet.tools.manager.InternetManager
import com.breadwallet.tools.manager.NetworkCallbacksConnectivityStateProvider
import com.breadwallet.tools.manager.RatesFetcher
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.CryptoUserManager
import com.breadwallet.tools.util.SupportManager
import com.breadwallet.ui.uigift.GiftBackup
import com.breadwallet.ui.uigift.SharedPrefsGiftBackup
import com.breadwallet.util.CryptoUriParser
import com.breadwallet.util.errorHandler
import com.platform.APIClient
import com.platform.interfaces.KVStoreProvider
import com.platform.interfaces.MetaDataManager
import com.platform.interfaces.WalletProvider
import com.platform.tools.KVStoreManager
import drewcarlson.blockset.BdbService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import java.io.File

/** All Application Dependencies */
fun getAppModule(application: BreadApp): DI.Module {
    return DI.Module("AppModule") {
        bind { singleton { CryptoUriParser(instance()) } }

        bind<APIClient> {
            singleton {
                APIClient(
                    context = application,
                    userManager = instance(),
                    brdPreferences = instance(),
                    okHttpClient = instance(),
                    headers = application.createHttpHeaders()
                )
            }
        }

        bind<BrdUserManager> {
            singleton {
                CryptoUserManager(
                    context = application,
                    createStore = { createCryptoEncryptedPrefs(application) },
                    metaDataProvider = instance(),
                    scope = instance()
                )
            }
        }

        bind<GiftBackup> {
            singleton { SharedPrefsGiftBackup { createGiftBackupEncryptedPrefs(application) } }
        }

        bind<KVStoreProvider> { singleton { KVStoreManager(application) } }

        val metaDataManager by lazy {
            MetaDataManager(application.direct.instance())
        }

        bind<WalletProvider> { singleton { metaDataManager } }

        bind<AccountMetaDataProvider> { singleton { metaDataManager } }

        bind<OkHttpClient>() with singleton {
            OkHttpClient().newBuilder().apply {
                // conditionally apply flipper interceptor on debug builds
                getFlipperOkhttpInterceptor()?.let {
                    addNetworkInterceptor(it)
                }
            }.build()
        }

        bind<BdbAuthInterceptor>() with singleton {
            val httpClient = instance<OkHttpClient>()
            BdbAuthInterceptor(
                httpClient = httpClient,
                userManager = instance(),
                scope = instance()
            )
        }

        bind<SystemClient>() with singleton {
            val httpClient = instance<OkHttpClient>()
            val authInterceptor = instance<BdbAuthInterceptor>()
            BlocksetSystemClient(
                httpClient.newBuilder()
                    .addInterceptor(authInterceptor)
                    .build()
            )
        }

        bind<CoroutineScope>(tag = "applicationScope") with singleton {
            CoroutineScope(
                SupervisorJob() + Dispatchers.Default + errorHandler("applicationScope")
            )
        }

        bind<HttpClient>() with singleton {
            HttpClient(OkHttp) {
                engine {
                    preconfigured = instance()
                    config {
                        retryOnConnectionFailure(true)
                    }
                }
            }
        }

        bind { singleton { BdbService.create(AndroidBdbAuthProvider(instance())) } }

        bind { singleton { AddressResolver(instance(), !BuildConfig.BITCOIN_TESTNET) } }

        bind<BreadBox>() with singleton {
            CoreBreadBox(
                storageFile = File(application.filesDir, WALLETKIT_DATA_DIR_NAME),
                isMainnet = !BuildConfig.BITCOIN_TESTNET,
                walletProvider = instance(),
                blockchainDb = instance(),
                userManager = instance()
            )
        }

        bind<ExperimentsRepository> { singleton { ExperimentsRepositoryImpl } }

        bind { singleton { RatesRepository.getInstance(application) } }

        bind { singleton { RatesFetcher(instance(), instance(), application) } }

        bind { singleton { ConversionTracker(instance()) } }

        bind { singleton { GiftTracker(instance(), instance()) } }

        bind<ConnectivityStateProvider>() with singleton {
            val connectivityManager =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NetworkCallbacksConnectivityStateProvider(connectivityManager)
            } else {
                InternetManager(connectivityManager, application)
            }
        }

        bind { singleton { SupportManager(application, instance(), instance()) } }

        bind<BrdApiClient>() with singleton {
            val brdPreferences = instance<BrdPreferences>()
            val customHost = brdPreferences.debugApiHost?.run(BrdApiHost::Custom)
            val host =
                customHost ?: BrdApiHost.hostFor(BuildConfig.DEBUG, brdPreferences.hydraActivated)
            BrdApiClient.create(host, AndroidBrdAuthProvider(instance()), instance())
        }

        bind<Preferences>() with singleton {
            val prefs =
                application.getSharedPreferences(BRSharedPrefs.PREFS_NAME, Context.MODE_PRIVATE)
            AndroidPreferences(prefs)
        }

        bind { singleton { BrdPreferences(instance()) } }

        bind { singleton { BakersApiClient.create(instance()) } }

        bind<ExchangeDataLoader>() with singleton {
            val exchangePrefs =
                application.getSharedPreferences("ExchangeDataLoader", Application.MODE_PRIVATE)
            ExchangeDataLoader(instance(), AndroidPreferences(exchangePrefs))
        }
    }
}

private const val ENCRYPTED_PREFS_FILE = "crypto_shared_prefs"
private const val ENCRYPTED_GIFT_BACKUP_FILE = "gift_shared_prefs"

private fun createCryptoEncryptedPrefs(application: BreadApp): SharedPreferences? =
    createEncryptedPrefs(ENCRYPTED_PREFS_FILE, application)

private fun createGiftBackupEncryptedPrefs(application: BreadApp): SharedPreferences? =
    createEncryptedPrefs(ENCRYPTED_GIFT_BACKUP_FILE, application)

private fun createEncryptedPrefs(fileName: String, application: BreadApp): SharedPreferences? {
    val masterKeys = try {
        MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    } catch (e: Throwable) {
        BRReportsManager.error("Failed to create Master Keys", e)
        return null
    }

    return try {
        EncryptedSharedPreferences.create(
            application,
            fileName,
            masterKeys,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Throwable) {
        BRReportsManager.error("Failed to create Encrypted Shared Preferences", e)
        null
    }
}
