package com.platform.sqlite;


import android.util.Log;

import com.platform.kvstore.CompletionObject;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 1/13/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class KVItem {
    public static final String TAG = KVItem.class.getName();

    public long version;
    public long remoteVersion;
    public String key;
    public byte[] value;
    public long time;
    public int deleted;
    public CompletionObject.RemoteKVStoreError err;

    public KVItem(long version, long remoteVersion, String key, byte[] value, long time, int deleted, CompletionObject.RemoteKVStoreError err) {
        this.version = version;
        this.remoteVersion = remoteVersion;
        this.key = key;
        this.value = value;
        this.time = time;
        this.deleted = deleted;
        this.err = err;
    }

    public KVItem(long version, long remoteVersion, String key, byte[] value, long time, int deleted) {
        this.version = version;
        this.remoteVersion = remoteVersion;
        this.key = key;
        this.value = value;
        this.time = time;
        this.deleted = deleted;
    }

    public void printValues() {
        Log.e(TAG, "KVItem values:");
        Log.e(TAG, "version: " + version);
        Log.e(TAG, "remoteVersion: " + remoteVersion);
        Log.e(TAG, "key: " + key);
        Log.e(TAG, "value.length: " + value.length);
        Log.e(TAG, "time: " + time);
        Log.e(TAG, "deleted: " + deleted);
    }

    private KVItem() {
    }

}
