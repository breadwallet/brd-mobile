/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 10/24/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.sqlite;

import android.database.sqlite.SQLiteDatabase;


public interface BRDataSourceInterface {

    SQLiteDatabase openDatabase();
    void closeDatabase();
}
