/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

import android.net.Uri
import com.breadwallet.R
import com.breadwallet.tools.util.Link
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.util.CurrencyCode
import dev.zacsweers.redacted.annotations.Redacted

object SettingsScreen {

    const val CONFIRM_EXPORT_TRANSACTIONS_DIALOG = "confirm_export"

    data class M(
        val section: SettingsSection,
        @Redacted val items: List<SettingsItem> = listOf(),
        val isLoading: Boolean = false
    ) {
        companion object {
            fun createDefault(section: SettingsSection) = M(section)
        }
    }

    sealed class E {

        data class OnLinkScanned(val link: Link) : E()
        data class OnOptionClicked(val option: SettingsOption) : E()

        data class OnOptionsLoaded(@Redacted val options: List<SettingsItem>) : E()

        object OnBackClicked : E()
        object OnCloseClicked : E()

        object OnAuthenticated : E()

        data class ShowPhrase(@Redacted val phrase: List<String>) : E()
        data class SetApiServer(val host: String) : E()
        data class SetPlatformDebugUrl(val url: String) : E()
        data class SetPlatformBundle(val bundle: String) : E()
        data class SetTokenBundle(val bundle: String) : E()
        object OnWalletsUpdated : E()
        object ShowHiddenOptions : E()
        object OnCloseHiddenMenu : E()

        data class OnATMMapClicked(val url: String, val mapJson: String) : E()

        object OnExportTransactionsConfirmed : E()
        data class OnTransactionsExportFileGenerated(val uri: Uri) : E()
    }

    sealed class F {
        object SendAtmFinderRequest : F()
        object SendLogs : F()
        object ViewLogs : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.LogcatViewer
        }
        object ViewMetadata : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.MetadataViewer
        }
        object ShowApiServerDialog : F(), ViewEffect
        object ShowPlatformDebugUrlDialog : F(), ViewEffect
        object ShowPlatformBundleDialog : F(), ViewEffect
        object ShowTokenBundleDialog : F(), ViewEffect
        object ResetDefaultCurrencies : F()
        object WipeNoPrompt : F()
        object GetPaperKey : F()
        object EnableAllWallets : F()
        object ClearBlockchainData : F()
        object ToggleRateAppPrompt : F()
        object RefreshTokens : F()
        object DetailedLogging : F()
        object CopyPaperKey : F()

        data class SetApiServer(val host: String) : F()
        data class SetPlatformDebugUrl(val url: String) : F()
        data class SetPlatformBundle(val bundle: String) : F()
        data class SetTokenBundle(val bundle: String) : F()
        data class LoadOptions(val section: SettingsSection) : F()

        object GoToOrderHistory : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.OrderHistory()
        }

        object GoToRegionPreferences : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.RegionPreferences
        }

        data class GoToSection(val section: SettingsSection) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Menu(section)
        }

        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }

        object GoToSupport : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SupportPage("")
        }

        object GoToQrScan : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.QRScanner
        }

        object GoToBrdRewards : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.BrdRewards
        }

        object GoToGooglePlay : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.ReviewBrd
        }

        object GoToAbout : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.About
        }

        object GoToDisplayCurrency : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DisplayCurrency
        }

        object GoToNotificationsSettings : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.NotificationsSettings
        }

        object GoToShareData : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.ShareDataSettings
        }

        object GoToImportWallet : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.ImportWallet()
        }

        data class GoToSyncBlockchain(
            val currencyCode: CurrencyCode
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SyncBlockchain(currencyCode)
        }

        object GoToNodeSelector : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.BitcoinNodeSelector
        }

        object GoToEnableSegWit : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.EnableSegWit
        }

        object GoToLegacyAddress : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.LegacyAddress
        }

        object GoToFingerprintAuth : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.FingerprintSettings
        }

        object GoToUpdatePin : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SetPin()
        }

        object GoToWipeWallet : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.WipeWallet
        }

        object GoToOnboarding : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.OnBoarding
        }

        object GoToNativeApiExplorer : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.NativeApiExplorer
        }

        object GoToHomeScreen : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }

        object GoToAuthentication : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Authentication()
        }

        data class GoToPaperKey(
            @Redacted val phrase: List<String>
        ) : F(), NavigationEffect {
            override val navigationTarget =
                NavigationTarget.PaperKey(phrase, null)
        }

        data class GoToFastSync(
            val currencyCode: CurrencyCode
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.FastSync(currencyCode)
        }

        data class GoToLink(val link: Link) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DeepLink(
                link = link,
                authenticated = true
            )
        }

        data class GoToATMMap(val url: String, val mapJson: String) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.ATMMap(url, mapJson)
        }

        object RelaunchHomeScreen : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }

        object ShowConfirmExportTransactions : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AlertDialog(
                titleResId = R.string.ExportConfirmation_title,
                messageResId = R.string.ExportConfirmation_message,
                positiveButtonResId = R.string.ExportConfirmation_continue,
                negativeButtonResId = R.string.ExportConfirmation_cancel,
                dialogId = CONFIRM_EXPORT_TRANSACTIONS_DIALOG
            )
        }

        object GenerateTransactionsExportFile : F()

        object EnableNativeExchangeUI : F()

        data class ExportTransactions(val uri: Uri) : F(), ViewEffect
    }
}
