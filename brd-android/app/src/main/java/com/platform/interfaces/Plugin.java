package com.platform.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/1/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public interface Plugin {
    public static final String TAG = Plugin.class.getName();

    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response);
}
