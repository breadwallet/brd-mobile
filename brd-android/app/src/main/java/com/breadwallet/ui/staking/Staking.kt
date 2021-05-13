/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.staking

import com.brd.bakerapi.models.Baker
import com.breadwallet.R
import com.blockset.walletkit.TransferFeeBasis
import com.breadwallet.ext.isZero
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import java.math.BigDecimal

object Staking {

    sealed class M {

        sealed class TransactionError {
            object FeeEstimateFailed : TransactionError()
            object TransferFailed : TransactionError()
            object Unknown : TransactionError()
        }

        abstract val currencyId: String
        abstract val currencyCode: String
        abstract val address: String
        abstract val balance: BigDecimal
        abstract val isAuthenticating: Boolean
        abstract val isFingerprintEnabled: Boolean
        abstract val bakers: List<Baker>
        abstract val isLoadingBakers: Boolean

        data class Loading(
            override val currencyCode: String = "",
            override val currencyId: String = "",
            override val address: String = "",
            override val balance: BigDecimal = BigDecimal.ZERO,
            override val isAuthenticating: Boolean = false,
            override val isFingerprintEnabled: Boolean = false,
            override val bakers: List<Baker> = emptyList(),
            override val isLoadingBakers: Boolean = false
        ) : M()

        data class SetValidator(
            override val currencyId: String,
            override val currencyCode: String,
            override val address: String,
            override val balance: BigDecimal = BigDecimal.ZERO,
            override val isAuthenticating: Boolean = false,
            override val isFingerprintEnabled: Boolean,
            override val bakers: List<Baker>,
            override val isLoadingBakers: Boolean = false,
            val originalAddress: String,
            val isAddressValid: Boolean,
            val isAddressChanged: Boolean,
            val canSubmitTransfer: Boolean,
            val transactionError: TransactionError?,
            val feeEstimate: TransferFeeBasis?,
            val isCancellable: Boolean,
            val confirmWhenReady: Boolean = false,
            val selectedBaker: Baker? = null,
        ) : M() {
            val isWalletEmpty: Boolean
                get() = selectedBaker != null && balance.isZero()

            companion object {
                fun createDefault(
                    balance: BigDecimal,
                    currencyId: String,
                    currencyCode: String,
                    originalAddress: String? = null,
                    isFingerprintEnabled: Boolean,
                    bakers: List<Baker> = emptyList(),
                    selectedBaker: Baker? = null,
                ) = SetValidator(
                    balance = balance,
                    currencyId = currencyId,
                    currencyCode = currencyCode,
                    address = "",
                    isAddressValid = true,
                    isAddressChanged = false,
                    canSubmitTransfer = false,
                    transactionError = null,
                    isCancellable = originalAddress != null,
                    originalAddress = originalAddress ?: "",
                    feeEstimate = null,
                    isFingerprintEnabled = isFingerprintEnabled,
                    bakers = bakers,
                    selectedBaker = selectedBaker,
                )
            }
        }

        data class ViewValidator(
            override val currencyId: String,
            override val currencyCode: String,
            override val address: String,
            override val balance: BigDecimal,
            override val isAuthenticating: Boolean = false,
            override val isFingerprintEnabled: Boolean = false,
            override val bakers: List<Baker> = emptyList(),
            override val isLoadingBakers: Boolean = false,
            val state: State,
            val feeEstimate: TransferFeeBasis? = null,
            val baker: Baker? = null,
        ) : M() {
            enum class State {
                PENDING_STAKE, PENDING_UNSTAKE, STAKED
            }
        }
    }

    sealed class E {

        sealed class AccountUpdated : E() {
            abstract val currencyCode: String
            abstract val balance: BigDecimal

            data class Unstaked(
                override val currencyCode: String,
                override val balance: BigDecimal
            ) : AccountUpdated()

            data class Staked(
                override val currencyCode: String,
                val address: String,
                val state: M.ViewValidator.State,
                override val balance: BigDecimal
            ) : AccountUpdated()
        }

        data class OnBakerChanged(val baker: Baker) : E()
        data class OnAddressValidated(
            val isValid: Boolean,
            val fromClipboard: Boolean = false,
        ) : E()

        data class OnTransferFailed(val transactionError: M.TransactionError) : E()
        data class OnFeeUpdated(
            val address: String?,
            val feeEstimate: TransferFeeBasis,
            val balance: BigDecimal
        ) : E()

        data class OnAuthenticationSettingsUpdated(val isFingerprintEnabled: Boolean) : E()

        data class OnBakersLoaded(val bakers: List<Baker>) : E()
        object OnBakersFailed : E()
        data class OnBakerLoaded(val baker: Baker) : E()
        object OnBakerFailed : E()

        object OnStakeClicked : E()
        object OnUnstakeClicked : E()
        object OnCancelClicked : E()
        object OnPasteClicked : E()
        object OnCloseClicked : E()
        object OnHelpClicked : E()
        object OnConfirmClicked : E()
        object OnTransactionConfirmClicked : E()
        object OnTransactionCancelClicked : E()
        object OnAuthSuccess : E()
        object OnAuthCancelled : E()
        object OnSelectBakerClicked : E()
    }

    sealed class F {

        object LoadAccount : F()
        object LoadAuthenticationSettings : F()
        data class PasteFromClipboard(
            val currentDelegateAddress: String
        ) : F()

        object Help : F(), ViewEffect
        object Close : F(), ViewEffect

        data class Unstake(val feeEstimate: TransferFeeBasis) : F()
        data class EstimateFee(val address: String?) : F()
        data class Stake(val address: String, val feeEstimate: TransferFeeBasis) : F()
        data class ValidateAddress(val address: String) : F()
        data class ConfirmTransaction(
            val currencyCode: String,
            val address: String?,
            val balance: BigDecimal,
            val fee: BigDecimal
        ) : F(), ViewEffect

        object LoadBakers : F()
        data class LoadBaker(val address: String) : F()
        data class GoToSelectBaker(val bakers: List<Baker>) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SelectBakerScreen(bakers)
        }

        object ShowBakerError : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AlertDialog(
                messageResId = R.string.Staking_loadingBakerError,
                positiveButtonResId = R.string.Button_ok
            )
        }
    }
}
