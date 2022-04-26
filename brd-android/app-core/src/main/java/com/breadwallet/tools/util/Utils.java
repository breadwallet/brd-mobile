package com.breadwallet.tools.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.content.Context.FINGERPRINT_SERVICE;


/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/21/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class Utils {
    public static final String TAG = Utils.class.getName();

    private static final String NUMBER_PATTERN = "-?\\d+(\\.\\d+)?";

    public static boolean isUsingCustomInputMethod(Context context) {
        if (context == null) return false;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
        final int N = mInputMethodProperties.size();
        for (int i = 0; i < N; i++) {
            InputMethodInfo imi = mInputMethodProperties.get(i);
            if (imi.getId().equals(
                    Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.DEFAULT_INPUT_METHOD))) {
                if ((imi.getServiceInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void printPhoneSpecs() {
        String specsTag = "PHONE SPECS";
        Log.e(specsTag, "");
        Log.e(specsTag, "***************************PHONE SPECS***************************");
        Log.e(specsTag, "* Build.CPU_ABI: " + Build.CPU_ABI);
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.e(specsTag, "* maxMemory:" + Long.toString(maxMemory));
        Log.e(specsTag, "----------------------------PHONE SPECS----------------------------");
        Log.e(specsTag, "");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(byte[] arr) {
        return arr == null || arr.length == 0;
    }

    public static int getPixelsFromDps(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
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

    public static boolean isFingerprintEnrolled(Context app) {
        FingerprintManager fingerprintManager = (FingerprintManager) app.getSystemService(FINGERPRINT_SERVICE);
        // Device doesn't support fingerprint authentication
        return ActivityCompat.checkSelfPermission(app, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
                && fingerprintManager != null
                && fingerprintManager.isHardwareDetected()
                && fingerprintManager.hasEnrolledFingerprints();
    }

    public static boolean isFingerprintAvailable(Context app) {
        FingerprintManager fingerprintManager = (FingerprintManager) app.getSystemService(FINGERPRINT_SERVICE);
        if (fingerprintManager == null) return false;
        // Device doesn't support fingerprint authentication
        if (ActivityCompat.checkSelfPermission(app, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(app, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
            return false;
        }
        return fingerprintManager.isHardwareDetected();
    }

    public static void hideKeyboard(Context app) {
        if (app != null) {
            Activity activity = (Activity) app;
            View view = activity.getCurrentFocus() != null ?
                    activity.getCurrentFocus() : activity.findViewById(android.R.id.content);
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    // This method checks if a screen altering app(such as Twightlight) is currently running
    // If it is, notify the user that the BRD app will not function properly and they should
    // disable it
    public static boolean checkIfScreenAlteringAppIsRunning(Context app, String packageName) {


        // Use the ActivityManager API if sdk version is less than 21
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Get the Activity Manager
            ActivityManager manager = (ActivityManager) app.getSystemService(Activity.ACTIVITY_SERVICE);

            // Get a list of running tasks, we are only interested in the last one,
            // the top most so we give a 1 as parameter so we only get the topmost.
            List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
            Log.d(TAG, "Process list count -> " + processes.size());


            String processName = "";
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {

                // Get the info we need for comparison.
                processName = processInfo.processName;
                Log.d(TAG, "Process package name -> " + processName);

                // Check if it matches our package name
                if (processName.equals(packageName)) return true;


            }

        }

        // Use the UsageStats API for sdk versions greater than Lollipop
        else {
            UsageStatsManager usm = (UsageStatsManager) app.getSystemService(Activity.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                String currentPackageName = "";
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    currentPackageName = usageStats.getPackageName();


                    if (currentPackageName.equals(packageName)) {
                        return true;
                    }

                }

            }

        }

        return false;
    }

    public static void correctTextSizeIfNeeded(TextView v) {
        int limit = 100;
        int lines = v.getLineCount();
        float px = v.getTextSize();
        while (lines > 1 && !v.getText().toString().contains("\n")) {
            limit--;
            px -= 1;
            v.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
            lines = v.getLineCount();
            if (limit <= 0) {
                Log.e(TAG, "correctTextSizeIfNeeded: Failed to rescale, limit reached, final: " + px);
                break;
            }
        }
    }

    /**
     * Helper function to validate if a string is in JSON format. Supports either JSONObject or JSONArray.
     *
     * @param jsonString The string to be validated.
     * @return True if valid JSON, false if not.
     */
    public static boolean isValidJSON(String jsonString) {
        try {
            new JSONObject(jsonString);
        } catch (JSONException e1) {
            try {
                new JSONArray(jsonString);
            } catch (JSONException e2) {
                return false;
            }
        }
        return true;
    }
}
