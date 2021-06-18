/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 12/09/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.uigift

import android.content.SharedPreferences
import androidx.core.content.edit

private const val KEY_GIFT_PREFIX = "gift-pk-"
private const val KEY_GIFT_STATE_PREFIX = "gift-state-"

data class GiftCopy(
    val address: String,
    val privateKey: String,
    val isUsed: Boolean = false
)

interface GiftBackup {
    fun putGift(gift: GiftCopy)
    fun removeUnusedGift(address: String)
    fun markGiftIsUsed(address: String)
    fun getAllGifts(): List<GiftCopy>
}

class SharedPrefsGiftBackup(
    private val createStore: () -> SharedPreferences?
) : GiftBackup {

    private val store: SharedPreferences? by lazy {
        createStore()
    }

    override fun putGift(gift: GiftCopy) {
        checkNotNull(store).edit {
            putString(gift.address.giftKey(), gift.privateKey)
            putBoolean(gift.address.giftStateKey(), gift.isUsed)
        }
    }

    override fun removeUnusedGift(address: String) {
        val store = checkNotNull(store)
        if (!store.getBoolean(address.giftStateKey(), false)) {
            store.edit {
                remove(address.giftKey())
                remove(address.giftStateKey())
            }
        }
    }

    override fun markGiftIsUsed(address: String) {
        checkNotNull(store).edit {
            putBoolean(address.giftStateKey(), true)
        }
    }

    override fun getAllGifts(): List<GiftCopy> {
        val all = checkNotNull(store).all
        return all.keys
            .filter { it.startsWith(KEY_GIFT_PREFIX) }
            .map { key ->
                val address = key.substringAfter(KEY_GIFT_PREFIX)
                GiftCopy(
                    address,
                    all[key] as String,
                    all[address.giftStateKey()] as Boolean
                )
            }
    }

    private fun String.giftKey() = "$KEY_GIFT_PREFIX$this"
    private fun String.giftStateKey() = "$KEY_GIFT_STATE_PREFIX$this"
}
