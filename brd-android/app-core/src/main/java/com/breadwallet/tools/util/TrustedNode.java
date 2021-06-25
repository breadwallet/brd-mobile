package com.breadwallet.tools.util;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/17/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class TrustedNode {
    public static  String getNodeHost(String input) {
        if (input.contains(":")) {
            return input.split(":")[0];
        }
        return input;
    }

    public static  int getNodePort(String input) {
        int port = 0;
        if (input.contains(":")) {
            try {
                port = Integer.parseInt(input.split(":")[1]);
            } catch (Exception e) {

            }
        }
        return port;
    }

    public static  boolean isValid(String input) {
        try {
            if (input == null || input.length() == 0) return false;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (!Character.isDigit(c) && c != '.' && c != ':') return false;
            }
            String host;
            if (input.contains(":")) {
                String[] pieces = input.split(":");
                if (pieces.length > 2) return false;
                host = pieces[0];
                int port = Integer.parseInt(pieces[1]); //just try to see if it's a number
            } else {
                host = input;
            }
            String[] nums = host.split("\\.");
            if (nums.length != 4) return false;
            for (int i = 0; i < nums.length; i++) {
                int slice = Integer.parseInt(nums[i]);
                if (slice < 0 || slice > 255) return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}