package com.platform.kvstore;

import com.platform.sqlite.KVItem;

import java.util.List;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/13/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class CompletionObject {
    public static final String TAG = CompletionObject.class.getName();

    public enum RemoteKVStoreError {
        notFound,
        conflict,
        tombstone,
        unknown
    }

    public KVItem kv;
    public long version;
    public long time;
    public RemoteKVStoreError err;
    public byte[] value;
    public List<KVItem> kvs;
    public String key;

    public CompletionObject(long version, long time, RemoteKVStoreError err) {
        this.version = version;
        this.time = time;
        this.err = err;
    }

    public CompletionObject(String key, long version, long time, RemoteKVStoreError err) {
        this.key = key;
        this.version = version;
        this.time = time;
        this.err = err;
    }

    public CompletionObject(long version, long time, byte[] value, RemoteKVStoreError err) {
        this.value = value;
        this.version = version;
        this.time = time;
        this.err = err;
    }

    public CompletionObject(List<KVItem> keys, RemoteKVStoreError err) {
        this.kvs = keys;
        this.err = err;
    }
    public CompletionObject(List<KVItem> keys) {
        this.kvs = keys;
    }

    public CompletionObject(KVItem key, RemoteKVStoreError err) {
        this.kv = key;
        this.err = err;
    }

    public CompletionObject(RemoteKVStoreError err) {
        this.err = err;
    }

}
