/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import com.breadwallet.app.ApplicationLifecycleObserver
import com.breadwallet.app.BreadApp
import com.blockset.walletkit.Account
import com.blockset.walletkit.Key
import com.breadwallet.BuildConfig
import com.breadwallet.logger.logInfo
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.tools.crypto.CryptoHelper.hexDecode
import com.breadwallet.tools.crypto.CryptoHelper.hexEncode
import com.breadwallet.tools.manager.BRReportsManager
import com.breadwallet.tools.manager.BRSharedPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import kotlin.math.pow

const val PIN_LENGTH = 6
const val LEGACY_PIN_LENGTH = 4
private const val MAX_UNLOCK_ATTEMPTS = 3
private const val JWT_EXP_PADDING_MS = 10_000L
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val NEW_CIPHER_ALGORITHM = "AES/GCM/NoPadding"

private const val POLL_ATTEMPTS_MAX = 15
private const val POLL_TIMEOUT_MS = 1000L

private const val PUT_PHRASE_RC = 400
private const val GET_PHRASE_RC = 401

private const val KEY_ACCOUNT = "account"
private const val KEY_PHRASE = "phrase"
private const val KEY_AUTH_KEY = "authKey"
private const val KEY_CREATION_TIME = "creationTimeSeconds"
private const val KEY_TOKEN = "token"
private const val KEY_BDB_JWT = "bdbJwt"
private const val KEY_BDB_JWT_EXP = "bdbJwtExp"
private const val KEY_PIN_CODE = "pinCode"
private const val KEY_FAIL_COUNT = "failCount"
private const val KEY_FAIL_TIMESTAMP = "failTimestamp"
private const val KEY_ETH_PUB_KEY = "ethPublicKey"

private const val MANUFACTURER_GOOGLE = "Google"

@Suppress("TooManyFunctions")
class CryptoUserManager(
    context: Context,
    private val createStore: () -> SharedPreferences?,
    private val metaDataProvider: AccountMetaDataProvider,
    private val scope: CoroutineScope,
) : BrdUserManager {

    private var store: SharedPreferences? = null

    private val keyguard = checkNotNull(context.getSystemService<KeyguardManager>())

    private val mutex = Mutex()

    private val resultChannel = Channel<Int>()
    private val stateChangeChannel = BroadcastChannel<Unit>(CONFLATED)

    private val accountInvalidated = AtomicBoolean()
    private val locked = AtomicBoolean(true)
    private val disabledSeconds = AtomicInteger(0)
    private var token: String? = null
    private var jwt: String? = null
    private var jwtExp: Long? = null

    init {
        store = createStore()
        if (store != null && isPhraseKeyValid()) {
            startDisabledTimer(getFailTimestamp())
        }
        stateChangeChannel.offer(Unit)

        ApplicationLifecycleObserver.addApplicationLifecycleListener { event ->
            if (event == Lifecycle.Event.ON_START) {
                if (getState() is BrdUserState.KeyStoreInvalid) {
                    stateChangeChannel.offer(Unit)
                }
            }
        }
    }

    override suspend fun setupWithGeneratedPhrase(): SetupResult {
        val creationDate = Date()
        val phrase = try {
            generatePhrase()
        } catch (e: Exception) {
            return SetupResult.FailedToGeneratePhrase(e)
        }
        return initAccount(phrase, creationDate).also {
            withContext(Dispatchers.IO) {
                metaDataProvider.create(creationDate)
            }
        }
    }

    override suspend fun setupWithPhrase(phrase: ByteArray): SetupResult {
        val apiKey = Key.createForBIP32ApiAuth(
            phrase,
            Key.getDefaultWordList()
        ).orNull() ?: return SetupResult.FailedToCreateApiKey

        checkNotNull(store).edit {
            putBytes(KEY_AUTH_KEY, apiKey.encodeAsPrivate())
        }
        val creationDate = metaDataProvider.walletInfo()
            .first()
            .creationDate
            .run(::Date)
        scope.launch {
            metaDataProvider.recoverAll(true)
        }
        return initAccount(phrase, creationDate, apiKey)
    }

    override suspend fun migrateKeystoreData() = mutex.withLock {
        // Migrate fields required for Account
        val phrase = getPhrase() ?: return@withLock false
        try {
            val creationDate = Date(BRKeyStore.getWalletCreationTime())
            val account =
                Account.createFromPhrase(phrase, creationDate, BRSharedPrefs.getDeviceId(), !BuildConfig.BITCOIN_TESTNET).get()
            val apiKey = Key.createForBIP32ApiAuth(
                phrase,
                Key.getDefaultWordList()
            ).orNull()

            checkNotNull(apiKey) { "Migration failed, failed to create Api Key." }

            writeAccount(account, apiKey, creationDate)
        } catch (e: Exception) {
            wipeAccount()
            return@withLock false
        }

        // Migrate other fields
        BRKeyStore.getToken()?.let { putToken(String(it)) }
        BRKeyStore.getEthPublicKey()?.let { putEthPublicKey(it) }
        putPinCode(BRKeyStore.getPinCode())
        putFailCount(BRKeyStore.getFailCount())
        putFailTimestamp(BRKeyStore.getFailTimeStamp())

        scope.launch(Dispatchers.IO) {
            BRKeyStore.wipeAfterMigration()
        }

        true
    }

    override suspend fun checkAccountInvalidated() {
        val deviceId = BRSharedPrefs.getDeviceId()
        val invalidated = checkNotNull(store).getBytes(KEY_ACCOUNT, null)?.let {
            Account.createFromSerialization(it, deviceId).orNull() == null
        } ?: false

        if (!invalidated) return
        val phrase = getPhrase()
        if (phrase == null) {
            accountInvalidated.set(true)
            stateChangeChannel.offer(Unit)
            return
        }

        logInfo("Account needs to be re-created from phrase.")

        val account = Account.createFromPhrase(
            phrase,
            Date(getWalletCreationTime()),
            deviceId,
            !BuildConfig.BITCOIN_TESTNET
        ).orNull()

        if (account == null) {
            accountInvalidated.set(true)
            stateChangeChannel.offer(Unit)
        } else {
            logInfo("Account re-created.")
            checkNotNull(store).edit {
                putBytes(KEY_ACCOUNT, account.serialize())
            }
        }
    }

    override fun isInitialized() = getState() != BrdUserState.Uninitialized

    override fun isMigrationRequired() =
        !isInitialized() &&
            (BRKeyStore.getMasterPublicKey() != null || BRKeyStore.hasAccountBytes())

    override fun getState(): BrdUserState = when {
        // Account invalidated and phrase not provided or phrase Key invalidated, recovery required
        accountInvalidated.get() || !isPhraseKeyValid() -> if (requiresUninstall()) {
            BrdUserState.KeyStoreInvalid.Uninstall
        } else {
            BrdUserState.KeyStoreInvalid.Wipe
        }
        // User must create/restore a wallet
        getAccount() == null -> BrdUserState.Uninitialized
        // Too many failed unauthorized access attempts, user must wait
        disabledSeconds.get() > 0 -> BrdUserState.Disabled(disabledSeconds.get())
        // State is valid, request authentication
        locked.get() -> BrdUserState.Locked
        // State is valid
        else -> BrdUserState.Enabled
    }

    override fun stateChanges(disabledUpdates: Boolean): Flow<BrdUserState> =
        stateChangeChannel.asFlow()
            .onStart { emit(Unit) }
            .map { getState() }
            .transformLatest { state ->
                if (state is BrdUserState.Disabled && disabledUpdates) {
                    emit(state)
                    while (disabledSeconds.get() > 0) {
                        delay(1000)
                        emit(state.copy(seconds = disabledSeconds.get()))
                    }
                } else emit(state)
            }
            .distinctUntilChanged()
            .flowOn(Default)

    override suspend fun getPhrase(): ByteArray? =
        store?.getBytes(KEY_PHRASE, null)
            ?: executeWithAuth { BRKeyStore.getPhrase(it, GET_PHRASE_RC) }
                ?.also { bytes -> store?.edit { putBytes(KEY_PHRASE, bytes) } }

    override fun getAccount(): Account? =
        store?.getBytes(KEY_ACCOUNT, null)?.run {
            Account.createFromSerialization(
                this,
                BRSharedPrefs.getDeviceId()
            ).orNull()
        }

    override fun updateAccount(accountBytes: ByteArray) {
        checkNotNull(
            Account.createFromSerialization(
                accountBytes,
                BRSharedPrefs.getDeviceId()
            ).orNull()
        )
        checkNotNull(store).edit { putBytes(KEY_ACCOUNT, accountBytes) }
    }

    override fun getAuthKey() = checkNotNull(store).getBytes(KEY_AUTH_KEY, null)

    override suspend fun configurePinCode(pinCode: String) {
        checkNotNull(getPhrase()) { "Phrase is null, cannot set pin code." }
        check(pinCode.length == PIN_LENGTH && pinCode.toIntOrNull() != null) {
            "Invalid pin code."
        }

        putPinCode(pinCode)
        putFailCount(0)
    }

    override suspend fun clearPinCode(phrase: ByteArray) {
        val storedPhrase = checkNotNull(getPhrase()) { "Phrase is null, cannot clear pin code." }
        check(phrase.contentEquals(storedPhrase)) { "Phrase does not match." }
        putPinCode("")
        putFailCount(0)
        putFailTimestamp(0)
        disabledSeconds.set(0)
        locked.set(false)
        stateChangeChannel.offer(Unit)
    }

    override fun verifyPinCode(pinCode: String) =
        if (pinCode == getPinCode()) {
            putFailCount(0)
            putFailTimestamp(0)
            locked.set(false)
            stateChangeChannel.offer(Unit)
            true
        } else {
            val failCount = getFailCount() + 1
            putFailCount(failCount)
            if (failCount >= MAX_UNLOCK_ATTEMPTS) {
                BRSharedPrefs.getSecureTime()
                    .also(::putFailTimestamp)
                    .also(::startDisabledTimer)
                stateChangeChannel.offer(Unit)
            }
            false
        }

    override fun hasPinCode() = getPinCode().isNotBlank()

    override fun pinCodeNeedsUpgrade() =
        getPinCode().let { it.isNotBlank() && it.length != PIN_LENGTH }

    override fun lock() {
        if (!locked.getAndSet(true)) {
            stateChangeChannel.offer(Unit)
        }
    }

    override fun unlock() {
        if (locked.getAndSet(false)) {
            stateChangeChannel.offer(Unit)
        }
    }

    @Synchronized
    override fun getToken() = token ?: checkNotNull(store).getString(KEY_TOKEN, null)

    @Synchronized
    override fun putToken(token: String) {
        checkNotNull(store).edit { putString(KEY_TOKEN, token) }
        this.token = token
    }

    @Synchronized
    override fun removeToken() {
        checkNotNull(store).edit { remove(KEY_TOKEN) }
        token = null
    }

    @Synchronized
    override fun getBdbJwt() =
        if ((getBdbJwtExp() - JWT_EXP_PADDING_MS) <= System.currentTimeMillis()) {
            null
        } else {
            jwt ?: checkNotNull(store).getString(KEY_BDB_JWT, null)
        }

    @Synchronized
    override fun putBdbJwt(jwt: String, exp: Long) {
        checkNotNull(store).edit {
            putString(KEY_BDB_JWT, jwt)
            putLong(KEY_BDB_JWT_EXP, exp)
        }
        this.jwt = jwt
        this.jwtExp = exp
    }

    @Synchronized
    private fun getBdbJwtExp() =
        jwtExp ?: checkNotNull(store).getLong(KEY_BDB_JWT_EXP, 0)

    /**
     * Used to determine address mismatch cases for tracking purposes, therefore exposed
     * as part of the implementation rather than the [BrdUserManager] interface.
     */
    fun getEthPublicKey(): ByteArray? =
        checkNotNull(store).getBytes(KEY_ETH_PUB_KEY, null)

    private fun generatePhrase(): ByteArray {
        val words = Key.getDefaultWordList()
        return Account.generatePhrase(words).also { phrase ->
            check(Account.validatePhrase(phrase, words)) {
                "Invalid phrase generated."
            }
        }
    }

    private suspend fun initAccount(phrase: ByteArray, creationDate: Date, apiKey: Key? = null) =
        mutex.withLock {
            check(getPhrase() == null) { "Phrase already exists." }

            try {
                val storedPhrase = checkNotNull(store).run {
                    edit { putBytes(KEY_PHRASE, phrase) }
                    getBytes(KEY_PHRASE, null)
                } ?: return@withLock SetupResult.FailedToPersistPhrase

                val account = Account.createFromPhrase(
                    storedPhrase,
                    creationDate,
                    BRSharedPrefs.getDeviceId(),
                    !BuildConfig.BITCOIN_TESTNET
                ).get() ?: return@withLock SetupResult.FailedToCreateAccount

                val apiAuthKey = apiKey ?: Key.createForBIP32ApiAuth(
                    phrase,
                    Key.getDefaultWordList()
                ).orNull() ?: return@withLock SetupResult.FailedToCreateApiKey

                writeAccount(account, apiAuthKey, creationDate)

                if (
                    !validateAccount(
                        storedPhrase,
                        account,
                        apiAuthKey,
                        creationDate.time
                    )
                ) return@withLock SetupResult.FailedToCreateValidWallet

                locked.set(false)
                stateChangeChannel.offer(Unit)
                logInfo("Account created.")
                SetupResult.Success
            } catch (e: Exception) {
                wipeAccount()
                SetupResult.UnknownFailure(e)
            }
        }

    private fun getPinCode() =
        checkNotNull(store).getString(KEY_PIN_CODE, "") ?: ""

    private fun putEthPublicKey(ethPubKey: ByteArray) =
        checkNotNull(store).edit { putBytes(KEY_ETH_PUB_KEY, ethPubKey) }

    private fun getWalletCreationTime() =
        checkNotNull(store).getLong(KEY_CREATION_TIME, 0L)

    private fun disabledUntil(failCount: Int, failTimestamp: Long): Long {
        val pow =
            PIN_LENGTH.toDouble()
                .pow((failCount - MAX_UNLOCK_ATTEMPTS).toDouble()) * DateUtils.MINUTE_IN_MILLIS
        return (failTimestamp + pow).toLong()
    }

    private fun putPinCode(pinCode: String) {
        checkNotNull(store).edit { putString(KEY_PIN_CODE, pinCode) }
    }

    private fun getFailCount() =
        checkNotNull(store).getInt(KEY_FAIL_COUNT, 0)

    private fun putFailCount(count: Int) =
        checkNotNull(store).edit { putInt(KEY_FAIL_COUNT, count) }

    private fun getFailTimestamp(): Long =
        checkNotNull(store).getLong(KEY_FAIL_TIMESTAMP, 0)

    private fun putFailTimestamp(timestamp: Long) =
        checkNotNull(store).edit { putLong(KEY_FAIL_TIMESTAMP, timestamp) }

    private fun startDisabledTimer(failTimestamp: Long) {
        if (failTimestamp > 0) {
            BRSharedPrefs.secureTimeFlow()
                .map { secureTime ->
                    val disableUntil = disabledUntil(getFailCount(), failTimestamp)
                    ((disableUntil - secureTime) / 1000).toInt()
                }
                .takeWhile { it > 0 }
                .onEach { initialDelay ->
                    disabledSeconds.set(initialDelay)
                    while (disabledSeconds.get() > 0) {
                        delay(1000)
                        disabledSeconds.getAndDecrement()
                    }

                    putFailTimestamp(0)
                    disabledSeconds.set(0)
                    stateChangeChannel.offer(Unit)
                }
                .launchIn(scope)
        }
    }

    /**
     * Invokes [action], catching [UserNotAuthenticatedException].
     * If authentication is required, it will be attempted and upon
     * success will invoke [action] again. If authentication fails,
     * null is returned.
     */
    private suspend fun <T> executeWithAuth(action: (context: Activity) -> T): T {
        val activity = getActivity()
        return try {
            runOnMain { action(activity) }
        } catch (e: UserNotAuthenticatedException) {
            logInfo("Attempting authentication")

            when (resultChannel.receive()) {
                Activity.RESULT_OK -> runOnMain { action(activity) }
                else -> throw e
            }
        }
    }

    private suspend fun <T> runOnMain(block: () -> T): T {
        return Main {
            try {
                block()
            } catch (e: Throwable) {
                e
            }
        }.let { if (it is Throwable) throw it else (it as T) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int) {
        check(!resultChannel.isClosedForSend)

        resultChannel.offer(
            when (requestCode) {
                PUT_PHRASE_RC -> resultCode
                GET_PHRASE_RC -> resultCode
                else -> return
            }
        )
    }

    private fun writeAccount(
        account: Account,
        apiKey: Key,
        creationDate: Date
    ) {
        checkNotNull(store).edit {
            putBytes(KEY_ACCOUNT, account.serialize())
            putBytes(KEY_AUTH_KEY, apiKey.encodeAsPrivate())
            putLong(KEY_CREATION_TIME, creationDate.time)
        }
    }

    private suspend fun validateAccount(
        phrase: ByteArray,
        account: Account,
        apiKey: Key,
        creationTime: Long
    ) = withContext(Dispatchers.IO) {
        Account.validatePhrase(phrase, Key.getDefaultWordList()) &&
            getAccount()?.serialize()?.contentEquals(account.serialize()) == true &&
            getAuthKey()?.contentEquals(apiKey.encodeAsPrivate()) == true &&
            getWalletCreationTime() == creationTime
    }

    @VisibleForTesting
    fun wipeAccount() {
        checkNotNull(store).edit {
            putString(KEY_PHRASE, null)
            putString(KEY_ACCOUNT, null)
            putString(KEY_AUTH_KEY, null)
            putString(KEY_PIN_CODE, null)
            putLong(KEY_CREATION_TIME, 0)
        }
        BRKeyStore.wipeKeyStore(true)
    }

    private suspend fun getActivity(): Activity {
        var context = BreadApp.getBreadContext()
        while (context !is Activity) {
            context = BreadApp.getBreadContext()
            delay(1_000)
        }
        return context
    }

    private fun isPhraseKeyValid(): Boolean {
        if (store?.getBytes(KEY_PHRASE, null) != null) {
            // BRKeyStore is not required anymore, phrase key is ignored.
            return true
        }

        return try {
            // Attempt to retrieve the key that protects the paper key and initialize an encryption cipher.
            val key = KeyStore.getInstance(ANDROID_KEY_STORE)
                .apply { load(null) }
                .getKey(BRKeyStore.PHRASE_ALIAS, null)

            when {
                key != null -> {
                    val cipher = Cipher.getInstance(NEW_CIPHER_ALGORITHM)
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                    true
                }
                store?.getBytes(KEY_ACCOUNT, null) != null -> false // key is null when it should not be
                else -> true // key has not been initialized, the key store is still considered valid
            }
        } catch (e: Exception) {
            // If KeyPermanentlyInvalidatedException
            //  -> with no cause happens, then the password was disabled. See DROID-1019.
            // If UnrecoverableKeyException
            //  -> with cause "Key blob corrupted" happens then the password was disabled & re-enabled. See DROID-1207.
            //  -> with cause "Key blob corrupted" happens then after DROID-1019 the app was opened again while password is on.
            //  -> with cause "Key not found" happens then after DROID-1019 the app was opened again while password is off.
            //  -> with cause "System error" (KeyStoreException) after app wipe on devices that need uninstall to recover.
            // Note: These exceptions would happen before a UserNotAuthenticatedException, so we don't need to handle that.
            (e !is KeyPermanentlyInvalidatedException && e !is UnrecoverableKeyException).also { isValid ->
                if (!isValid) BRReportsManager.error("Phrase key permanently invalidated", e)
            }
        }
    }

    private fun requiresUninstall(): Boolean {
        val isGoogleDevice = MANUFACTURER_GOOGLE == Build.MANUFACTURER
        val isOmr1 = Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1
        val isOorAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        return isGoogleDevice && isOmr1 || !isGoogleDevice && isOorAbove
    }
}

fun SharedPreferences.getBytes(key: String, defaultValue: ByteArray?): ByteArray? {
    val valStr = getString(key, null) ?: return defaultValue
    return hexDecode(valStr)
}

fun SharedPreferences.Editor.putBytes(key: String, value: ByteArray) {
    putString(key, hexEncode(value))
}
