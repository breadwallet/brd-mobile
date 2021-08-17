/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import kotlin.native.concurrent.SharedImmutable

object Formatters {

    fun new(): NumberFormatter {
        return NumberFormatter(CommonLocales.root)
    }

    fun fiat(currencyCode: String): NumberFormatter {
        val localeId = currencyToLocaleMap[currencyCode.lowercase()] ?: "en_US"
        return fiat(currencyCode, CommonLocales.forId(localeId))
    }

    fun fiat(currencyCode: String, locale: CommonLocale): NumberFormatter {
        return NumberFormatter(locale, currencyFormatter = true).also { formatter ->
            formatter.currencyCode = currencyCode
        }
    }

    fun crypto(currencyCode: String): NumberFormatter {
        return NumberFormatter(CommonLocales.root).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 8
            this.currencyCode = currencyCode
        }
    }
}

expect class NumberFormatter constructor(locale: CommonLocale, currencyFormatter: Boolean = false) {

    var minimumFractionDigits: Int
    var maximumFractionDigits: Int
    var currencyCode: String
    var currencySymbol: String?
    var alwaysShowDecimalSeparator: Boolean

    fun format(double: Double): String
}

// NOTE: Usage on the JVM requires replacing instances of '_' with '-'.
@SharedImmutable
internal val currencyToLocaleMap = mapOf(
    "afn" to "fa_AF",
    "all" to "sq",
    "dzd" to "ar_DZ",
    "usd" to "en_US",
    "eur" to "de_DE",
    "aoa" to "pt_AO",
    "xcd" to "en_VC",
    "ars" to "es_AR",
    "amd" to "hy",
    "awg" to "nl_AW",
    "aud" to "en_AU",
    "azn" to "az",
    "bsd" to "en_BS",
    "bhd" to "ar_BH",
    "bdt" to "bn_BD",
    "bbd" to "en_BB",
    "byn" to "be",
    "bzd" to "en_BZ",
    "xof" to "fr_TG",
    "bmd" to "en_BM",
    "inr" to "hi_IN",
    "bob" to "es_BO",
    "bam" to "bs",
    "bwp" to "en_BW",
    "nok" to "nb_NO",
    "brl" to "pt_BR",
    "bnd" to "ms_BN",
    "bgn" to "bg",
    "bif" to "fr_BI",
    "cve" to "pt_CV",
    "khr" to "km",
    "xaf" to "fr_GA",
    "cad" to "en_CA",
    "kyd" to "en_KY",
    "clp" to "es_CL",
    "cny" to "zh_Hans_CN",
    "hkd" to "zh",
    "mop" to "zh",
    "cop" to "es_CO",
    "kmf" to "ar",
    "nzd" to "en_NZ",
    "crc" to "es_CR",
    "hrk" to "hr_HR",
    "cup" to "es_CU",
    "ang" to "nl",
    "czk" to "cs",
    "kpw" to "ko_KP",
    "cdf" to "fr_CD",
    "dkk" to "kl",
    "djf" to "fr_DJ",
    "dop" to "es_DO",
    "egp" to "ar_EG",
    "svc" to "es_SV",
    "ern" to "ar_ER",
    "szl" to "en_SZ",
    "etb" to "am",
    "fjd" to "en_FJ",
    "xpf" to "fr_PF",
    "gmd" to "en_GM",
    "gel" to "ka",
    "ghs" to "en_GH",
    "gip" to "en_GI",
    "gtq" to "es_GT",
    "gbp" to "en_GB",
    "gnf" to "fr_GN",
    "gyd" to "en_GY",
    "htg" to "fr_HT",
    "hnl" to "es_HN",
    "huf" to "hu_HU",
    "isk" to "is",
    "idr" to "id",
    "irr" to "fa_IR",
    "iqd" to "ar_IQ",
    "ils" to "he",
    "jmd" to "en_JM",
    "jpy" to "ja_JP",
    "jod" to "ar_JO",
    "kzt" to "kk",
    "kes" to "en_KE",
    "kwd" to "ar_KW",
    "kgs" to "ky",
    "lak" to "lo",
    "lbp" to "ar_LB",
    "lsl" to "en_LS",
    "lrd" to "en_LR",
    "lyd" to "ar_LY",
    "chf" to "de_CH",
    "mga" to "fr_MG",
    "mwk" to "ny",
    "myr" to "ms_MY",
    "mvr" to "dv",
    "mru" to "ar_MR",
    "mur" to "en_MU",
    "mxn" to "es_MX",
    "mnt" to "mn",
    "mad" to "ar",
    "mzn" to "pt_MZ",
    "mmk" to "my",
    "nad" to "en_NA",
    "npr" to "ne",
    "nio" to "es_NI",
    "ngn" to "en_NG",
    "omr" to "ar_OM",
    "pkr" to "en_PK",
    "pab" to "es_PA",
    "pgk" to "en_PG",
    "pyg" to "es_PY",
    "pen" to "es_PE",
    "php" to "pt_TL",
    "pln" to "pl_PL",
    "qar" to "ar_QA",
    "krw" to "ko_KR",
    "mdl" to "ro",
    "ron" to "ro",
    "rub" to "ru",
    "rwf" to "rw",
    "shp" to "en_SH",
    "wst" to "en_AS",
    "stn" to "pt_ST",
    "sar" to "ar_SA",
    "rsd" to "sr",
    "scr" to "en_SC",
    "sll" to "en_SL",
    "sgd" to "ar_SD",
    "sbd" to "en_SB",
    "sos" to "so_SO",
    "zar" to "zu",
    "ssp" to "en",
    "lkr" to "si",
    "sdg" to "ar_SD",
    "srd" to "nl_SR",
    "sek" to "sv_SE",
    "syp" to "ar_SY",
    "tjs" to "tg",
    "thb" to "th",
    "mkd" to "mk",
    "top" to "to",
    "ttd" to "en_TT",
    "tnd" to "ar_TN",
    "try" to "tr_TR",
    "tmt" to "tk",
    "ugx" to "en_UG",
    "uah" to "uk",
    "aed" to "ar_AE",
    "tzs" to "sw_TZ",
    "uyu" to "es_UY",
    "uzs" to "uz",
    "vuv" to "en_BI",
    "ves" to "es_VE",
    "vnd" to "vi_VN",
    "yer" to "ar_YE",
    "zmw" to "en_ZM",
    "zwl" to "en_ZW",
)
