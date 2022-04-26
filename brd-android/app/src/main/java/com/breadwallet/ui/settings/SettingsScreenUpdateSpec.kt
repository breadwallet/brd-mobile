/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

import com.spotify.mobius.Next

interface SettingsScreenUpdateSpec {
    fun patch(model: SettingsScreen.M, event: SettingsScreen.E): Next<SettingsScreen.M, SettingsScreen.F> = when (event) {
        SettingsScreen.E.OnBackClicked -> onBackClicked(model)
        SettingsScreen.E.OnCloseClicked -> onCloseClicked(model)
        SettingsScreen.E.OnAuthenticated -> onAuthenticated(model)
        SettingsScreen.E.OnWalletsUpdated -> onWalletsUpdated(model)
        SettingsScreen.E.ShowHiddenOptions -> showHiddenOptions(model)
        SettingsScreen.E.OnCloseHiddenMenu -> onCloseHiddenMenu(model)
        SettingsScreen.E.OnExportTransactionsConfirmed -> onExportTransactionsConfirmed(model)
        is SettingsScreen.E.OnLinkScanned -> onLinkScanned(model, event)
        is SettingsScreen.E.OnOptionClicked -> onOptionClicked(model, event)
        is SettingsScreen.E.OnOptionsLoaded -> onOptionsLoaded(model, event)
        is SettingsScreen.E.ShowPhrase -> showPhrase(model, event)
        is SettingsScreen.E.SetApiServer -> setApiServer(model, event)
        is SettingsScreen.E.SetPlatformDebugUrl -> setPlatformDebugUrl(model, event)
        is SettingsScreen.E.SetPlatformBundle -> setPlatformBundle(model, event)
        is SettingsScreen.E.SetTokenBundle -> setTokenBundle(model, event)
        is SettingsScreen.E.OnATMMapClicked -> onATMMapClicked(model, event)
        is SettingsScreen.E.OnTransactionsExportFileGenerated -> onTransactionsExportFileGenerated(model, event)
    }

    fun onBackClicked(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onCloseClicked(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onAuthenticated(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onWalletsUpdated(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun showHiddenOptions(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onCloseHiddenMenu(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onLinkScanned(model: SettingsScreen.M, event: SettingsScreen.E.OnLinkScanned): Next<SettingsScreen.M, SettingsScreen.F>

    fun onOptionClicked(model: SettingsScreen.M, event: SettingsScreen.E.OnOptionClicked): Next<SettingsScreen.M, SettingsScreen.F>

    fun onOptionsLoaded(model: SettingsScreen.M, event: SettingsScreen.E.OnOptionsLoaded): Next<SettingsScreen.M, SettingsScreen.F>

    fun showPhrase(model: SettingsScreen.M, event: SettingsScreen.E.ShowPhrase): Next<SettingsScreen.M, SettingsScreen.F>

    fun setApiServer(model: SettingsScreen.M, event: SettingsScreen.E.SetApiServer): Next<SettingsScreen.M, SettingsScreen.F>

    fun setPlatformDebugUrl(model: SettingsScreen.M, event: SettingsScreen.E.SetPlatformDebugUrl): Next<SettingsScreen.M, SettingsScreen.F>

    fun setPlatformBundle(model: SettingsScreen.M, event: SettingsScreen.E.SetPlatformBundle): Next<SettingsScreen.M, SettingsScreen.F>

    fun setTokenBundle(model: SettingsScreen.M, event: SettingsScreen.E.SetTokenBundle): Next<SettingsScreen.M, SettingsScreen.F>

    fun onATMMapClicked(model: SettingsScreen.M, event: SettingsScreen.E.OnATMMapClicked): Next<SettingsScreen.M, SettingsScreen.F>

    fun onExportTransactionsConfirmed(model: SettingsScreen.M): Next<SettingsScreen.M, SettingsScreen.F>

    fun onTransactionsExportFileGenerated(model: SettingsScreen.M, event: SettingsScreen.E.OnTransactionsExportFileGenerated): Next<SettingsScreen.M, SettingsScreen.F>
}
