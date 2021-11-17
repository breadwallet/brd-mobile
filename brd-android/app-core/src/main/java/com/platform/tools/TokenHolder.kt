/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.tools

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import android.util.Log
import com.brd.api.BrdApiClient
import com.breadwallet.logger.logError

import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.BrdUserState
import kotlinx.coroutines.runBlocking
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

@SuppressLint("StaticFieldLeak")
object TokenHolder : DIAware {
    private val TAG = TokenHolder::class.java.simpleName
    private var mApiToken: String? = null
    private var mOldApiToken: String? = null
    private lateinit var context: Context

    fun provideContext(context: Context) {
        this.context = context
    }

    override val di by closestDI { context }
    private val userManager by instance<BrdUserManager>()
    private val brdApiClient: BrdApiClient by instance()

    @Synchronized
    fun retrieveToken(): String? {
        //If token is not present
        if (mApiToken.isNullOrBlank()) {
            //Check BrdUserManager
            val token = when (userManager.getState()) {
                BrdUserState.Enabled, BrdUserState.Locked ->
                    userManager.getToken()
                else -> null
            }
            //Not in the BrdUserManager, update from server.
            if (token.isNullOrEmpty()) {
                fetchNewToken()
            } else {
                mApiToken = token
            }
        }
        return mApiToken
    }

    @Synchronized
    fun updateToken(expiredToken: String?): String? {
        if (mOldApiToken == null || mOldApiToken != expiredToken) {
            Log.e(TAG, "updateToken: updating the token")
            mOldApiToken = mApiToken
            fetchNewToken()
        }
        return mApiToken
    }

    @Synchronized
    fun fetchNewToken() {
        mApiToken = runBlocking {
            try {
                brdApiClient.getToken()
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
        logError("fetchNewToken: $mApiToken")
        if (!mApiToken.isNullOrEmpty()) {
            userManager.putToken(mApiToken!!)
        }
    }

    @VisibleForTesting
    @Synchronized
    fun reset() {
        mOldApiToken = null
    }
}
