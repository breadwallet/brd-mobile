/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/15/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.receive

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.receive.ReceiveScreen.E
import com.breadwallet.ui.receive.ReceiveScreen.E.OnAmountChange.AddDecimal
import com.breadwallet.ui.receive.ReceiveScreen.E.OnAmountChange.AddDigit
import com.breadwallet.ui.receive.ReceiveScreen.E.OnAmountChange.Delete
import com.breadwallet.ui.receive.ReceiveScreen.F
import com.breadwallet.ui.receive.ReceiveScreen.M
import com.breadwallet.ui.send.MAX_DIGITS
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import java.math.BigDecimal

@Suppress("TooManyFunctions")
object ReceiveUpdate : Update<M, E, F>, ReceiveScreenUpdateSpec {
    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onWalletInfoLoaded(model: M, event: E.OnWalletInfoLoaded): Next<M, F> =
        next(
            model.copy(
                receiveAddress = event.address,
                sanitizedAddress = event.sanitizedAddress,
                walletName = event.walletName
            )
        )

    override fun onCloseClicked(model: M): Next<M, F> =
        dispatch(setOf(F.CloseSheet))

    override fun onFaqClicked(model: M): Next<M, F> =
        dispatch(setOf(F.GoToFaq(model.currencyCode)))

    override fun onCopyAddressClicked(model: M): Next<M, F> =
        dispatch(
            setOf<F>(
                F.CopyAddressToClipboard(model.receiveAddress),
                F.ShowCopiedMessage
            )
        )

    override fun onShareClicked(model: M): Next<M, F> =
        dispatch(
            setOf(
                F.ShareRequest(
                    model.receiveAddress,
                    model.amount,
                    model.walletName
                )
            )
        )

    override fun onAmountClicked(model: M): Next<M, F> {
        return next(
            model.copy(
                isAmountEditVisible = !model.isAmountEditVisible
            )
        )
    }

    override fun onAmountChange(
        model: M,
        event: E.OnAmountChange
    ): Next<M, F> {
        return when (event) {
            AddDecimal -> addDecimal(model)
            Delete -> delete(model)
            is AddDigit -> addDigit(model, event)
        }
    }

    private fun addDecimal(
        model: M
    ): Next<M, F> {
        return when {
            model.rawAmount.contains('.') -> noChange()
            model.rawAmount.isEmpty() -> next(
                model.copy(rawAmount = "0.")
            )
            else -> next(
                model.copy(
                    rawAmount = model.rawAmount + '.'
                )
            )
        }
    }

    private fun delete(
        model: M
    ): Next<M, F> {
        return when {
            model.rawAmount.isEmpty() -> noChange()
            else -> next(
                model.withNewRawAmount(model.rawAmount.dropLast(1))
            )
        }
    }

    private fun addDigit(
        model: M,
        event: AddDigit
    ): Next<M, F> {
        return when {
            model.rawAmount == "0" && event.digit == 0 -> noChange()
            model.rawAmount.split('.').run {
                // Ensure the length of the main or fraction
                // if present are less than MAX_DIGITS
                if (size == 1) first().length == MAX_DIGITS
                else getOrNull(1)?.length == MAX_DIGITS
            } -> noChange()
            else -> {
                val newRawAmount = when (model.rawAmount) {
                    "0" -> event.digit.toString()
                    else -> model.rawAmount + event.digit
                }
                next(model.withNewRawAmount(newRawAmount))
            }
        }
    }

    override fun onToggleCurrencyClicked(model: M): Next<M, F> {
        if (model.fiatPricePerUnit == BigDecimal.ZERO) {
            return noChange()
        }

        val isAmountCrypto = !model.isAmountCrypto
        if (model.rawAmount.isBlank()) {
            return next(model.copy(isAmountCrypto = isAmountCrypto))
        }
        return next(
            model.copy(
                isAmountCrypto = isAmountCrypto,
                rawAmount = when {
                    isAmountCrypto -> model.amount.toPlainString()
                    else ->
                        model.fiatAmount
                            .setScale(2, BRConstants.ROUNDING_MODE)
                            .toPlainString()
                }
            )
        )
    }

    override fun onExchangeRateUpdated(
        model: M,
        event: E.OnExchangeRateUpdated
    ): Next<M, F> {
        val pricePerUnit = event.fiatPricePerUnit
        val newAmount: BigDecimal
        val newFiatAmount: BigDecimal

        if (model.isAmountCrypto) {
            newAmount = model.amount
            newFiatAmount = if (pricePerUnit > BigDecimal.ZERO) {
                model.amount.setScale(2, BRConstants.ROUNDING_MODE) * pricePerUnit
            } else {
                model.fiatAmount
            }
        } else {
            newFiatAmount = model.fiatAmount
            newAmount = (
                model.fiatAmount.setScale(
                    model.amount.scale().coerceIn(2..MAX_DIGITS),
                    BRConstants.ROUNDING_MODE
                ) / pricePerUnit
                )
        }

        return next(
            model.copy(
                fiatPricePerUnit = pricePerUnit,
                fiatAmount = newFiatAmount,
                amount = newAmount
            )
        )
    }
}
