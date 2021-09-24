/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

/**
 * [CommonLocale] represents a region using the target platform's native
 * Locale representation.
 *
 * [CommonLocale] is generally available to configure high level APIs
 * with the platform "root" locale instance or the user's currently
 * selected locale.  This allows Jvm/Native Apple targets to typealias
 * the corresponding platform type but requires any common functionality
 * to be introduced with expected extension methods.
 */
expect class CommonLocale

expect val CommonLocale.currencyCode: String

expect val CommonLocale.countryCode: String


/**
 * [CommonLocale] provider for the [root] and [current] locales.
 */
expect object CommonLocales {

    /**
     * The "root" or system-locale free of region intricacies.
     */
    val root: CommonLocale

    /**
     * The "default" or user-selected locale.
     */
    val current: CommonLocale

    /**
     * Returns the Locale for [countryCode].
     */
    fun forCountryCode(countryCode: String): CommonLocale

    /**
     * Returns the Locale for [localeId].
     */
    fun forId(localeId: String): CommonLocale

    /**
     * Returns all available Locale ids on the system.
     */
    fun allIds(): List<String>
}
