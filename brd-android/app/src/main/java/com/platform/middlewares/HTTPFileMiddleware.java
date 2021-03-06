package com.platform.middlewares;

import android.content.Context;
import android.util.Log;

import com.breadwallet.app.BreadApp;
import com.breadwallet.tools.crypto.CryptoHelper;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.ServerBundlesHelper;
import com.breadwallet.tools.util.TypesConverter;
import com.breadwallet.tools.util.Utils;
import com.platform.APIClient;
import com.platform.BRHTTPHelper;
import com.platform.interfaces.Middleware;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import okhttp3.Request;


/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 10/17/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class HTTPFileMiddleware implements Middleware {
    public static final String TAG = HTTPFileMiddleware.class.getName();
    private static String DEBUG_URL = null; //modify for testing, "http://bw-platform-tests.s3-website.us-east-2.amazonaws.com" - for tests

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (target.equals("/")) return false;
        if (target.equals("/favicon.ico")) {
            APIClient.BRResponse resp = new APIClient.BRResponse(null, 200, null);

            return BRHTTPHelper.handleSuccess(resp, baseRequest, response);
        } else if (target.equals("/_didload")) {
            APIClient.BRResponse resp = new APIClient.BRResponse(null, 200, null);

            return BRHTTPHelper.handleSuccess(resp, baseRequest, response);
        }

        Context context = BreadApp.getBreadContext();
        if (context == null) {
            Log.e(TAG, "handle: app is null!");
            return true;
        }
//        if (Utils.isEmulatorOrDebug(app))
//            DEBUG_URL = "http://bw-platform-tests.s3-website.us-east-2.amazonaws.com";

        File temp = null;
        APIClient.BRResponse brResp = new APIClient.BRResponse();

        // Platform Debug URL may be set above or via shared preferences
        String webPlatformDebugURL = DEBUG_URL != null ? DEBUG_URL : BRSharedPrefs.getWebPlatformDebugURL();
        if (Utils.isNullOrEmpty(webPlatformDebugURL)) {
            // fetch the file locally
            String requestedFile = ServerBundlesHelper.getExtractedPath(context, ServerBundlesHelper.getBundle(ServerBundlesHelper.Type.WEB), target);
            Log.d(TAG, "Request local file -> " + requestedFile);
            Log.d(TAG, "Request local file target -> " + target);

            temp = new File(requestedFile);
            if (temp.exists() && !temp.isDirectory()) {
                Log.d(TAG, "handle: found bundle for:" + target);
            } else {
                Log.d(TAG, "handle: no bundle found for: " + target);
                return false;
            }

            Log.i(TAG, "handling: " + target + " " + baseRequest.getMethod());
            boolean modified = true;
            byte[] md5 = CryptoHelper.md5(TypesConverter.long2byteArray(temp.lastModified()));
            String hexEtag = Utils.bytesToHex(md5);
            response.setHeader("ETag", hexEtag);

            // if the client sends an if-none-match header, determine if we have a newer version of the file
            String etag = request.getHeader("if-none-match");
            if (etag != null && etag.equalsIgnoreCase(hexEtag)) modified = false;

            if (modified) {
                try {
                    brResp.setBody(FileUtils.readFileToByteArray(temp));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Utils.isNullOrEmpty(brResp.getBody())) {
                    return BRHTTPHelper.handleError(400, "could not read the file", baseRequest, response);
                }
            } else {
                APIClient.BRResponse resp = new APIClient.BRResponse(null, 304);

                return BRHTTPHelper.handleSuccess(resp, baseRequest, response);
            }
            response.setContentType(detectContentType(temp));
            brResp.setContentType(detectContentType(temp));

        } else {
            // download the file from the debug endpoint
            webPlatformDebugURL += target;

            Request debugRequest = new Request.Builder()
                    .url(webPlatformDebugURL)
                    .get().build();
            brResp = APIClient.getInstance(context).sendRequest(debugRequest, false);
        }

        brResp.setCode(200);

        String rangeString = request.getHeader("range");
        if (!Utils.isNullOrEmpty(rangeString)) {
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            return handlePartialRequest(baseRequest, response, temp);
        } else {
            if (Utils.isNullOrEmpty(brResp.getBody())) {
                return BRHTTPHelper.handleError(404, "not found", baseRequest, response);
            } else {

                return BRHTTPHelper.handleSuccess(brResp, baseRequest, response);
            }
        }

    }

    private boolean handlePartialRequest(org.eclipse.jetty.server.Request request, HttpServletResponse response, File file) {
        try {
            String rangeHeader = request.getHeader("range");
            String rangeValue = rangeHeader.trim()
                    .substring("bytes=".length());
            int fileLength = (int) file.length();
            int start, end;
            if (rangeValue.startsWith("-")) {
                end = fileLength - 1;
                start = fileLength - 1
                        - Integer.parseInt(rangeValue.substring("-".length()));
            } else {
                String[] range = rangeValue.split("-");
                start = Integer.parseInt(range[0]);
                end = range.length > 1 ? Integer.parseInt(range[1])
                        : fileLength - 1;
            }
            if (end > fileLength - 1) {
                end = fileLength - 1;
            }
            if (start <= end) {
                int contentLength = end - start + 1;
                response.setHeader("Content-Length", contentLength + "");
                response.setHeader("Content-Range", "bytes " + start + "-"
                        + end + "/" + fileLength);
                byte[] respBody = Arrays.copyOfRange(FileUtils.readFileToByteArray(file), start, contentLength);
                APIClient.BRResponse resp = new APIClient.BRResponse(respBody, 206, detectContentType(file));

                return BRHTTPHelper.handleSuccess(resp, request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                request.setHandled(true);
                response.getWriter().write("Invalid Range Header");
                response.sendError(400, "Bad Request");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return true;
        }
        return BRHTTPHelper.handleError(500, "unknown error", request, response);
    }

    private String detectContentType(File file) {
        String extension = FilenameUtils.getExtension(file.getAbsolutePath());
        switch (extension) {
            case "ttf":
                return "application/font-truetype";
            case "woff":
                return "application/font-woff";
            case "otf":
                return "application/font-opentype";
            case "svg":
                return "image/svg+xml";
            case "html":
                return "text/html";
            case "png":
                return "image/png";
            case "jpeg":
                return "image/jpeg";
            case "jpg":
                return "image/jpeg";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8;
            default:
                break;
        }
        return "application/octet-stream";
    }
}
