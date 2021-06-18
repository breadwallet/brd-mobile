package com.breadwallet.tools.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 9/28/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class TypesConverter {

    private TypesConverter() {
    }

    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    public static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        return Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
    }

    public static char[] lowerCaseCharArray(char[] arr) {
        char[] lowerPhrase = new char[arr.length];
        for (int i = 0; i < arr.length; i++) {
            lowerPhrase[i] = Character.toLowerCase(arr[i]);
        }
        return lowerPhrase;
    }

    public static char[] toChars(byte[] arr) {
        char[] charArray = new char[arr.length];
        for (int i = 0; i < arr.length; i++)
            charArray[i] = (char) arr[i];
        return charArray;
    }

    public static byte[] long2byteArray(long l) {
        byte b[] = new byte[8];

        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.putLong(l);
        return b;
    }

    public static long byteArray2long(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        return buf.getLong();
    }

    public static byte[] charsToBytes(char[] chars) {
        ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] array = new byte[buf.limit()];
        buf.get(array);
        return buf.array();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] hexToBytesReverse(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[(len - i - 1) / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
