/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 11/24/202.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.auth

enum class AuthMode {
    /** Attempt biometric auth if configured, otherwise the pin is required. */
    USER_PREFERRED,
    /** Ensures the use of a pin, fails immediately if not set. */
    PIN_REQUIRED,
    /** Ensures the use of biometric auth, fails immediately if not available. */
    BIOMETRIC_REQUIRED
}
