package com.platform;


import com.breadwallet.tools.util.Utils;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 2/16/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class BRHTTPHelper {
    public static final String TAG = BRHTTPHelper.class.getName();

    public static boolean handleError(int err, String errMess, Request baseRequest, HttpServletResponse resp) {
        try {
            baseRequest.setHandled(true);
            if (Utils.isNullOrEmpty(errMess))
                resp.sendError(err);
            else
                resp.sendError(err, errMess);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean handleSuccess(APIClient.BRResponse brResp, Request baseRequest, HttpServletResponse resp) {
        try {
            if (brResp.getCode() == 0) throw new RuntimeException("http code can't be 0");
            resp.setStatus(brResp.getCode());
            resp.setContentType(brResp.getContentType());
            if (!Utils.isNullOrEmpty(brResp.getBody()))
                resp.getOutputStream().write(brResp.getBody());
            for (String key : brResp.getHeaders().keySet()) {
                resp.setHeader(key, brResp.getHeaders().get(key));
            }
            baseRequest.setHandled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static byte[] getBody(HttpServletRequest request) {
        if (request == null) return null;
        byte[] rawData = null;
        try {
            InputStream body = request.getInputStream();
            rawData = IOUtils.toByteArray(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawData;
    }
}
