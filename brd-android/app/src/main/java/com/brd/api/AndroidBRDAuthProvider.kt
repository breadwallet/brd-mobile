/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brd.api

import com.breadwallet.crypto.Key
import com.breadwallet.logger.logError
import com.breadwallet.tools.crypto.CryptoHelper
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.platform.APIClient
import java.io.IOException
import java.util.Date
import java.util.TimeZone

private const val GMT = "GMT"

class AndroidBrdAuthProvider(
    private val userManager: BrdUserManager,
) : BrdAuthProvider {

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
        val hexDecodedKey = CryptoHelper.hexDecode(encodedPublicKey) ?: encodedPublicKey.toByteArray(Charsets.UTF_8)
        return CryptoHelper.base58Encode(hexDecodedKey)
    }

    override fun deviceId(): String {
        return BRSharedPrefs.getDeviceId()
    }

    override fun sign(method: String, body: String, contentType: String, url: String): BrdAuthProvider.Signature {
        val base58Body = try {
            if (body.isNotBlank()) {
                CryptoHelper.base58ofSha256(body.toByteArray(Charsets.UTF_8))
            } else ""
        } catch (e: IOException) {
            logError("authenticateRequest: ", e)
            ""
        }

        APIClient.DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
        val httpDate = APIClient.DATE_FORMAT.format(Date())
            .run { substring(0, indexOf(GMT) + GMT.length) }

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
}
