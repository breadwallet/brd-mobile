/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/11/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.security

sealed class SetupResult {
    object Success : SetupResult()
    object PhraseAlreadyExists : SetupResult()
    data class FailedToGeneratePhrase(val exception: Exception?) : SetupResult()
    object FailedToPersistPhrase : SetupResult()
    object FailedToCreateAccount : SetupResult()
    object FailedToCreateApiKey : SetupResult()
    object FailedToCreateValidWallet : SetupResult()
    data class UnknownFailure(val exception: Exception) : SetupResult()
}