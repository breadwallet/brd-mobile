/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.interfaces

import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/** Provides access to a Key-Value Store serving [JSONObject]. */
interface KVStoreProvider {
    /** Get value for [key]. */
    fun get(key: String): JSONObject?

    /** Put [value] for [key]. */
    fun put(key: String, value: JSONObject): Boolean

    fun getKeys(): List<String>

    /** Syncs the value for [key] and returns it, null if sync failed. */
    suspend fun sync(key: String): JSONObject?

    /** Syncs entire data store.*/
    suspend fun syncAll(syncOrder: List<String> = listOf()): Boolean

    /** Returns a [Flow] for a given [key]. */
    fun keyFlow(key: String): Flow<JSONObject>

    fun keysFlow(): Flow<List<String>>
}
