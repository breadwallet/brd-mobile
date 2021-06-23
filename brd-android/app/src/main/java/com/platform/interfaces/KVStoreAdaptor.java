/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/13/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.interfaces;

import com.platform.kvstore.CompletionObject;

/**
 * Interface for the available operations with server's KV store.
 */
public interface KVStoreAdaptor {

    /**
     * Return the most recent version of the key in the server.
     *
     * @param key
     * @return
     */
    CompletionObject ver(String key);

    /**
     * Save a value under a key. If it's a new key, then pass 0 as the If-None-Match header, otherwise you must
     * pass the current version in the database (which may be retrieved with KVStoreAdaptor.get).
     *
     * @param key     Existing key or 0 if it's a new key.
     * @param value   Value to be stored.
     * @param version Current version in the database.
     * @return
     */
    CompletionObject put(String key, byte[] value, long version);

    /**
     * Mark a key as deleted in the KV store.
     *
     * @param key     The key to save the data under
     * @param version Version of the key.
     * @return
     */
    CompletionObject del(String key, long version);

    /**
     * Retrieve a value from a key. If the given version is older than the server's version conflict will be returned.
     *
     * @param key     The key to retrieve from the KV store
     * @param version Current version in the database.
     * @return
     */
    CompletionObject get(String key, long version);

    /**
     * Retrieve all the keys from the server belonging to the current user.
     *
     * @return
     */
    CompletionObject keys();

}
