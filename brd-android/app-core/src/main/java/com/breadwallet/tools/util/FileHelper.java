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
    private static final String CORE_DATA_FILE_NAME = "coreData";
    private static String mCoreDataFilePath;

    public static void printDirectoryTree(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        Log.e(TAG, folder.getAbsolutePath());
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(folder, indent, sb);
        Log.e(TAG, sb.toString());
    }

    private static void printDirectoryTree(File folder, int indent,
                                           StringBuilder sb) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(folder.getName());
        sb.append("/");
        sb.append("\n");
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                printDirectoryTree(file, indent + 1, sb);
            } else {
                printFile(file, indent + 1, sb);
            }
        }

    }

    private static void printFile(File file, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(file.getName());
        sb.append("\n");
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }


    public static  File saveToExternalStorage(Context context, String fileName, String data) {
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

    /**
     * Returns the core data file full path.
     * @param context The context.
     * @return The file path.
     */
    public synchronized static String getCoreDataFilePath(Context context) {
        if (Utils.isNullOrEmpty(mCoreDataFilePath)) {
            // Create the storage file for core to save currencies data.
            File storageFile = new File(context.getFilesDir(), CORE_DATA_FILE_NAME);
            mCoreDataFilePath = storageFile.getAbsolutePath();
            storageFile.mkdirs();
        }
        return mCoreDataFilePath;
    }
}
