/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/28/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.crypto

import android.text.format.DateUtils
import com.blockset.walletkit.Coder
import com.blockset.walletkit.Hasher
import com.blockset.walletkit.Key
import com.blockset.walletkit.Signer

import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("TooManyFunctions")
object CryptoHelper {

    private const val NONCE_SIZE = 12

    private val base58: Coder by lazy {
        Coder.createForAlgorithm(Coder.Algorithm.BASE58)
    }

    private val sha256: Hasher by lazy {
        Hasher.createForAlgorithm(Hasher.Algorithm.SHA256)
    }

    private val sha256_2: Hasher by lazy {
        Hasher.createForAlgorithm(Hasher.Algorithm.SHA256_2)
    }

    private val md5: Hasher by lazy {
        Hasher.createForAlgorithm(Hasher.Algorithm.MD5)
    }

    private val keccak256: Hasher by lazy {
        Hasher.createForAlgorithm(Hasher.Algorithm.KECCAK256)
    }

    private val compact: Signer by lazy {
        Signer.createForAlgorithm(Signer.Algorithm.COMPACT)
    }

    private val jose: Signer by lazy {
        Signer.createForAlgorithm(Signer.Algorithm.BASIC_JOSE)
    }

    private val basicDer: Signer by lazy {
        Signer.createForAlgorithm(Signer.Algorithm.BASIC_DER)
    }

    private val hex: Coder by lazy {
        Coder.createForAlgorithm(Coder.Algorithm.HEX)
    }

    @JvmStatic
    fun hexEncode(data: ByteArray): String {
        return hex.encode(data).or("")
    }

    @JvmStatic
    fun hexDecode(data: String): ByteArray? {
        return hex.decode(data).orNull()
    }

    fun signCompact(data: ByteArray, key: Key): ByteArray {
        return compact.sign(data, key).or(byteArrayOf())
    }

    fun signJose(data: ByteArray, key: Key): ByteArray {
        return jose.sign(data, key).or(byteArrayOf())
    }

    fun signBasicDer(data: ByteArray, key: Key): ByteArray {
        return basicDer.sign(data, key).or(byteArrayOf())
    }

    fun base58Encode(data: ByteArray): String {
        return base58.encode(data).or("")
    }

    fun base58Decode(data: String): ByteArray {
        return base58.decode(data).or(byteArrayOf())
    }

    @JvmStatic
    fun base58ofSha256(toEncode: ByteArray): String {
        val sha256First = sha256(toEncode)
        return base58.encode(sha256First).or("")
    }

    @JvmStatic
    fun doubleSha256(data: ByteArray): ByteArray? {
        return sha256_2.hash(data).orNull()
    }

    @JvmStatic
    fun sha256(data: ByteArray?): ByteArray? {
        return sha256.hash(data).orNull()
    }

    @JvmStatic
    fun md5(data: ByteArray): ByteArray? {
        return md5.hash(data).orNull()
    }

    fun keccak256(data: ByteArray): ByteArray? {
        return keccak256.hash(data).orNull()
    }

    /**
     * generate a nonce using microseconds-since-epoch
     */
    @JvmStatic
    @Suppress("MagicNumber")
    fun generateRandomNonce(): ByteArray {
        val nonce = ByteArray(NONCE_SIZE)
        val buffer = ByteBuffer.allocate(8)
        val t = System.nanoTime() / DateUtils.SECOND_IN_MILLIS
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(t)
        val byteTime = buffer.array()
        System.arraycopy(byteTime, 0, nonce, 4, byteTime.size)
        return nonce
    }
}
