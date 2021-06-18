package com.breadwallet.tools.util;

import android.content.Context;
import android.util.Log;

import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/28/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class CurrencyUtils {
    public static final String TAG = CurrencyUtils.class.getName();
    private static final String KRONE = "DKK";
    private static final String POUND = "GBP";
    private static final String EURO = "EUR";

    /**
     * @param app                       - the Context
     * @param iso                       - the iso for the currency we want to format the amount for
     * @param amount                    - the smallest denomination currency (e.g. dollars or satoshis)
     * @param maxDecimalPlacesForCrypto - max decimal places to use or -1 for wallet's default
     * @return - the formatted amount e.g. $535.50 or b5000
     */
    @Deprecated
    public static String getFormattedAmount(Context app, String iso, BigDecimal amount, int maxDecimalPlacesForCrypto) {
        // TODO: Once callers have been migrated to getFormattedCryptoAmount or getFormattedFiatAmount, delete this
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (Utils.isNullOrEmpty(iso)) throw new RuntimeException("need iso for formatting!");
        DecimalFormat currencyFormat;
        // This formats currency values as the user expects to read them (default locale).
        currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        // This specifies the actual currency that the value is in, and provide
        // s the currency symbol.
        DecimalFormatSymbols decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setRoundingMode(BRConstants.ROUNDING_MODE);
        /* TODO getCryptoForSmallestCrypto
        amount = wallet.getCryptoForSmallestCrypto(app, amount);
        decimalFormatSymbols.setCurrencySymbol("");
        currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        currencyFormat.setMaximumFractionDigits(maxDecimalPlacesForCrypto == -1 ? wallet.getMaxDecimalPlaces(app) : maxDecimalPlacesForCrypto);
        currencyFormat.setMinimumFractionDigits(0);
        return String.format("%s %s", currencyFormat.format(amount), iso.toUpperCase());
         */
        try {
            Currency currency = Currency.getInstance(iso);
            String symbol = currency.getSymbol();
            decimalFormatSymbols.setCurrencySymbol(symbol);
            currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            currencyFormat.setNegativePrefix("-" + symbol);
            currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Currency not found for " + iso, e);
            BRReportsManager.reportBug(new IllegalArgumentException("Illegal currency code: " + iso));
        }
        return currencyFormat.format(amount);
    }

    /**
     * Returns a formatted fiat amount using the locale-specific format
     *
     * @param currencyCode the fiat currency code
     * @param amount the amount to format
     * @return the formatted string
     */
    public static String getFormattedFiatAmount(String currencyCode, BigDecimal amount) {
        // This formats currency values as the user expects to read them (default locale)
        DecimalFormat currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        DecimalFormatSymbols decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setRoundingMode(BRConstants.ROUNDING_MODE);
        try {
            Currency currency = Currency.getInstance(currencyCode);
            String symbol = currency.getSymbol();
            decimalFormatSymbols.setCurrencySymbol(symbol);
            currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            currencyFormat.setNegativePrefix("-" + symbol);
            currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Currency code: " + currencyCode);
            BRReportsManager.reportBug(new IllegalArgumentException("illegal iso: " + currencyCode));
        }
        return currencyFormat.format(amount);
    }

    public static boolean isBuyNotificationNeeded() {
        String fiatCurrencyCode = BRSharedPrefs.getPreferredFiatIso();
        return KRONE.equalsIgnoreCase(fiatCurrencyCode) || POUND.equalsIgnoreCase(fiatCurrencyCode) || EURO.equalsIgnoreCase(fiatCurrencyCode);
    }
}
