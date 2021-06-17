package com.platform.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import okhttp3.Request;
import okhttp3.Response;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 10/18/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public interface Middleware {

    boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response);

}
