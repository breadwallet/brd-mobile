package com.platform.middlewares;

import android.util.Log;

import com.platform.interfaces.Middleware;
import com.platform.interfaces.Plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 10/19/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class HTTPRouter extends SignedRequestMiddleware {
    public static final String TAG = HTTPRouter.class.getName();
    Set<Plugin> plugins;

    public HTTPRouter() {
        plugins = new LinkedHashSet<>();
    }

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (!super.handle(target, baseRequest, request, response)) {
            return false;
        }
        for (Plugin plugin : plugins) {
            boolean success = plugin.handle(target, baseRequest, request, response);
            if (success) {
                Log.i(TAG, "plugin: " + plugin.getClass().getName().substring(plugin.getClass().getName().lastIndexOf(".") + 1) + " succeeded:" + request.getRequestURL());
                return true;
            }
        }
        return false;
    }

    public void appendPlugin(Plugin plugin) {
        plugins.add(plugin);
    }
}
