/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.blockset.walletkit.Key
import com.breadwallet.logger.logError
import com.breadwallet.tools.crypto.CryptoHelper
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.platform.APIClient
import java.io.IOException
import java.util.Date

private const val GMT = "GMT"
private const val BRD_TOKEN_KEY = "BDB_TOKEN"
private const val BRD_TOKEN_STAGING_KEY = "BRD_TOKEN_STAGING"

class AndroidBrdAuthProvider(
    private val userManager: BrdUserManager,
) : BrdAuthProvider.Base() {

    private var authKey: Key? = null
        get() {
            if (field == null) {
                val key = userManager.getAuthKey() ?: byteArrayOf()
                if (key.isNotEmpty()) {
                    field = Key.createFromPrivateKeyString(key).orNull()
                }
            }
            return field
        }

    override var token: String?
        get() = userManager.getToken()
        set(value) {
            value?.run(userManager::putToken)
        }

    override fun hasKey(): Boolean {
        return userManager.getAuthKey() != null
    }

    override fun publicKey(): String {
        val encodedPublicKey = checkNotNull(authKey).encodeAsPublic().toString(Charsets.UTF_8)
        val hexDecodedKey =
            CryptoHelper.hexDecode(encodedPublicKey) ?: encodedPublicKey.toByteArray(Charsets.UTF_8)
        return CryptoHelper.base58Encode(hexDecodedKey)
    }

    override fun deviceId(): String {
        return BRSharedPrefs.getDeviceId()
    }

    override fun sign(
        method: String,
        body: String,
        contentType: String,
        url: String
    ): BrdAuthProvider.Signature {
        val base58Body = try {
            if (body.isNotBlank()) {
                CryptoHelper.base58ofSha256(body.toByteArray(Charsets.UTF_8))
            } else ""
        } catch (e: IOException) {
            logError("authenticateRequest: ", e)
            ""
        }

        val httpDate = APIClient.formatGmtDate(Date())
            ?.run { substring(0, indexOf(GMT) + GMT.length) }
            .orEmpty()

        val requestString = """
            $method
            $base58Body
            $contentType
            $httpDate
            $url
        """.trimIndent()

        return BrdAuthProvider.Signature(
            signature = checkNotNull(APIClient.signRequest(requestString, checkNotNull(authKey))),
            timestamp = httpDate
        )
    }

    override fun clientToken(): String? {
        return null
    }

    override fun walletId(): String? {
        return BRSharedPrefs.getWalletRewardId()
    }
}
