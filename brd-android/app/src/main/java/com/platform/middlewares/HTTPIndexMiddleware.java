package com.platform.middlewares;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Preconditions;
import com.breadwallet.app.BreadApp;
import com.breadwallet.tools.util.ServerBundlesHelper;
import com.platform.APIClient;
import com.platform.BRHTTPHelper;
import com.platform.interfaces.Middleware;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
public class HTTPIndexMiddleware implements Middleware {
    public static final String TAG = HTTPIndexMiddleware.class.getName();

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        Log.i(TAG, "handling: " + target + " " + baseRequest.getMethod());
        Context app = BreadApp.getBreadContext();
        if (app == null) {
            Log.e(TAG, "handle: app is null!");
            return true;
        }

        String indexFile = ServerBundlesHelper.getExtractedPath(app, ServerBundlesHelper.getBundle(ServerBundlesHelper.Type.WEB), rTrim(target, "/") + "/index.html");

        File temp = new File(indexFile);
        if (!temp.exists()) {
//            Log.d(TAG, "handle: FILE DOES NOT EXIST: " + temp.getAbsolutePath());
            return false;
        }

        try {
            byte[] body = FileUtils.readFileToByteArray(temp);
            Preconditions.checkNotNull(body);
            Preconditions.checkState(body.length != 0);
            response.setHeader("Content-Length", String.valueOf(body.length));
            APIClient.BRResponse resp = new APIClient.BRResponse(body, 200, "text/html;charset=utf-8");

            return BRHTTPHelper.handleSuccess(resp, baseRequest, response);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "handle: error sending response: " + target + " " + baseRequest.getMethod());
            return BRHTTPHelper.handleError(500, null, baseRequest, response);
        }

    }

    public String rTrim(String str, String piece) {
        if (str.endsWith(piece)) {
            return str.substring(str.lastIndexOf(piece), str.length());
        }
        return str;
    }
}
