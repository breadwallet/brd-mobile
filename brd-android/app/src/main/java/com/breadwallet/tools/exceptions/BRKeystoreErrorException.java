package com.breadwallet.tools.exceptions;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/20/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class BRKeystoreErrorException extends Exception {
    public static final String TAG = BRKeystoreErrorException.class.getName();

    public BRKeystoreErrorException(String message) {
        super(message);
    }
}
