package com.breadwallet.tools.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 9/28/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class BytesUtil {

    public static final int BUFFER_CAPACITY = 1024;

    public static byte[] readBytesFromStream(InputStream in) {

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        byte[] buffer = new byte[BUFFER_CAPACITY];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        try {
            while ((len = in.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();

    }
}
