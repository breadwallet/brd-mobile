/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 8/1/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.home

import com.breadwallet.R
import com.breadwallet.model.InAppMessage
import com.breadwallet.model.PriceChange
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.OnCompleteAction
import com.breadwallet.ui.settings.SettingsSection
import dev.zacsweers.redacted.annotations.Redacted
import java.math.BigDecimal

object HomeScreen {
    data class M(
        val wallets: Map<String, Wallet> = emptyMap(),
        val displayOrder: List<String> = emptyList(),
        val promptId: PromptItem? = null,
        val hasInternet: Boolean = true,
        val isBuyBellNeeded: Boolean = false,
        val isBuyPromoDotNeeded: Boolean = false,
        val isTradePromoDotNeeded: Boolean = false,
        val showBuyAndSell: Boolean = false,
        val rateAppPromptDontShowMeAgain: Boolean = false
    ) {

        companion object {
            fun createDefault() = M()
        }

        val aggregatedFiatBalance: BigDecimal = wallets.values
            .fold(BigDecimal.ZERO) { acc, next ->
                acc.add(next.fiatBalance)
            }

        val showPrompt: Boolean = promptId != null
    }

    sealed class E {
        data class OnEnabledWalletsUpdated(@Redacted val wallets: List<Wallet>) : E()

        data class OnWalletsUpdated(@Redacted val wallets: List<Wallet>) : E()

        data class OnWalletDisplayOrderUpdated(@Redacted val displayOrder: List<String>) : E()

        data class OnBuyBellNeededLoaded(val isBuyBellNeeded: Boolean) : E()

        data class OnBuyPromoDotNeededLoaded(val isBuyPromoDotNeeded: Boolean) : E()

        data class OnTradePromoDotNeededLoaded(val isTradePromoDotNeeded: Boolean) : E()

        data class OnConnectionUpdated(val isConnected: Boolean) : E()

        data class OnWalletClicked(val currencyCode: String) : E()

        object OnAddWalletsClicked : E()

        object OnBuyClicked : E()
        object OnTradeClicked : E()
        object OnMenuClicked : E()

        data class OnDeepLinkProvided(val url: String) : E()
        data class OnInAppNotificationProvided(val inAppMessage: InAppMessage) : E()

        data class OnPromptLoaded(val promptId: PromptItem?) : E()

        data class OnPushNotificationOpened(val campaignId: String) : E()

        data class OnShowBuyAndSell(val showBuyAndSell: Boolean) : E()

        object CheckForPrompt : E()
        data class OnPromptDismissed(val promptId: PromptItem) : E()
        object OnFingerprintPromptClicked : E()
        object OnPaperKeyPromptClicked : E()
        object OnUpgradePinPromptClicked : E()
        object OnRescanPromptClicked : E()
        object OnRateAppPromptClicked : E()
        data class OnRateAppPromptDontShowClicked(val checked: Boolean) : E()
        object OnRateAppPromptNoThanksClicked : E()
        data class OnEmailPromptClicked(@Redacted val email: String) : E()
        data class OnSupportFormSubmitted(val feedback: String) : E()
    }

    sealed class F {

        object LoadWallets : F()
        object LoadEnabledWallets : F()
        object LoadIsBuyBellNeeded : F()
        object LoadIsBuyPromoDotNeeded : F()
        object LoadIsTradePromoDotNeeded : F()
        object LoadPrompt : F()
        object LoadConnectivityState : F()
        object CheckInAppNotification : F()
        object CheckIfShowBuyAndSell : F()

        data class GoToDeepLink(val url: String) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DeepLink(url, true)
        }

        data class GoToInappMessage(val inAppMessage: InAppMessage) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.GoToInAppMessage(inAppMessage)
        }

        data class GoToWallet(val currencyCode: String) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Wallet(currencyCode)
        }

        object GoToAddWallet : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AddWallet
        }

        object GoToBuy : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Buy
        }

        object GoToTrade : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Trade
        }

        object GoToMenu : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Menu(SettingsSection.HOME)
        }

        object GoToFingerprintSettings : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.FingerprintSettings
        }

        object GoToWriteDownKey : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.WriteDownKey(
                OnCompleteAction.GO_HOME
            )
        }

        object GoToUpgradePin : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SetPin()
        }

        object GoToGooglePlay : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.ReviewBrd
        }

        object GoToSupportForm : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AlertDialog(
                titleResId = R.string.SupportForm_helpUsImprove,
                messageResId = R.string.SupportForm_feedbackAppreciated,
                positiveButtonResId = R.string.Button_submit,
                negativeButtonResId = R.string.SupportForm_notNow,
                textInputPlaceholderResId = R.string.SupportForm_pleaseDescribe
            )
        }

        data class RecordPushNotificationOpened(val campaignId: String) : F()

        data class UpdateWalletOrder(
            val orderedCurrencyIds: List<String>
        ) : F()

        data class TrackEvent(
            val eventName: String,
            val attributes: Map<String, String>? = null
        ) : F()

        data class DismissPrompt(val promptItem: PromptItem) : F()

        object StartRescan : F()

        data class SaveEmail(@Redacted val email: String) : F()

        object ClearRateAppPrompt : F()
        object SaveDontShowMeRateAppPrompt : F()

        data class SubmitSupportForm(val feedback: String) : F()
    }
}

data class Wallet(
    val currencyId: String,
    val currencyName: String,
    val currencyCode: String,
    val fiatPricePerUnit: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val fiatBalance: BigDecimal = BigDecimal.ZERO,
    val isSyncing: Boolean = false,
    val priceChange: PriceChange? = null,
    val state: State = State.READY,
    val startColor: String? = null,
    val endColor: String? = null,
    val isSupported: Boolean = true
) {
    enum class State {
        READY, LOADING, UNINITIALIZED
    }

    val hasPricePerUnit: Boolean = fiatPricePerUnit != BigDecimal.ZERO
}

enum class PromptItem {
    EMAIL_COLLECTION,
    FINGER_PRINT,
    PAPER_KEY,
    UPGRADE_PIN,
    RECOMMEND_RESCAN,
    RATE_APP
}
