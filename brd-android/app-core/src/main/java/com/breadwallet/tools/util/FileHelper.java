package com.breadwallet.tools.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 9/22/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class FileHelper {

    private static final String TAG = FileHelper.class.getName();

    public static File saveToExternalStorage(Context context, String fileName, String data) {
        File file = new File(context.getExternalCacheDir(), fileName);
        file.setReadable(true, false);
        try {
            boolean createdSuccessfully = file.createNewFile();
            if (!createdSuccessfully) {
                Log.e(TAG, "saveToExternalStorage: createNewFile: failed");
            }
        } catch (IOException e) {
            Log.e(TAG, "saveToExternalStorage: ", e);
        }
        Log.d(TAG, "saveToExternalStorage: " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "saveToExternalStorage: ", e);
        }
        return file;
    }
}
