/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/2/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.send

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.breadwallet.R
import com.breadwallet.breadbox.TransferSpeed
import com.breadwallet.breadbox.formatCryptoForUi
import com.breadwallet.databinding.ControllerConfirmTradeDetailsBinding
import com.breadwallet.tools.util.eth
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.changehandlers.DialogChangeHandler
import com.breadwallet.util.isErc20
import java.math.BigDecimal

private const val KEY_CURRENCY_CODE = "currency_code"
private const val KEY_TARGET_ADDRESS = "target_address"
private const val KEY_TRANSFER_SPEED = "transfer_speed"
private const val KEY_AMOUNT = "amount"
private const val KEY_NETWORK_FEE = "fiat_network_fee"
private const val KEY_TRANSFER_FIELDS = "transfer_fields"

/**
 * Transaction detail to be shown for user verification before requesting authentication.
 */
class ConfirmTradeController(
    args: Bundle? = null
) : BaseController(args) {

    interface Listener {
        fun onPositiveClicked() = Unit
        fun onNegativeClicked() = Unit
    }

    constructor(
        currencyCode: String,
        targetAddress: String,
        transferSpeed: TransferSpeed,
        amount: BigDecimal,
        networkFee: BigDecimal,
        transferFields: List<TransferField>
    ) : this(
        bundleOf(
            KEY_CURRENCY_CODE to currencyCode,
            KEY_TARGET_ADDRESS to targetAddress,
            KEY_TRANSFER_SPEED to transferSpeed.toString(),
            KEY_AMOUNT to amount,
            KEY_NETWORK_FEE to networkFee,
            KEY_TRANSFER_FIELDS to transferFields
        )
    )

    val model = ConfirmTradeModel(
        arg(KEY_CURRENCY_CODE),
        arg(KEY_TARGET_ADDRESS),
        TransferSpeed.valueOf(arg(KEY_TRANSFER_SPEED)),
        BigDecimal(arg<Double>(KEY_AMOUNT)),
        BigDecimal(arg<Double>(KEY_NETWORK_FEE)),
        arg(KEY_TRANSFER_FIELDS)
    )

    override val layoutId = R.layout.controller_confirm_tx_details

    init {
        overridePushHandler(DialogChangeHandler())
        overridePopHandler(DialogChangeHandler())
    }

    private val binding by viewBinding(ControllerConfirmTradeDetailsBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            okBtn.setOnClickListener {
                findListener<Listener>()?.onPositiveClicked()
            }
            val cancelTxListener = View.OnClickListener {
                findListener<Listener>()?.onNegativeClicked()
            }
            cancelBtn.setOnClickListener(cancelTxListener)
            closeBtn.setOnClickListener(cancelTxListener)
            layoutBackground.setOnClickListener(cancelTxListener)
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        model.render()
    }

    override fun handleBack(): Boolean {
        findListener<Listener>()?.onNegativeClicked()
        return super.handleBack()
    }

    private fun ConfirmTradeModel.render() {
        val res = checkNotNull(resources)
        with (binding) {
            val feeCode = if (currencyCode.isErc20()) eth else currencyCode

            sendValue.text = amount.formatCryptoForUi(currencyCode)
            toAddress.text = targetAddress
            networkFeeValue.text = networkFee.formatCryptoForUi(feeCode)


            val processingTime = res.getString(
                when {
                    currencyCode != feeCode -> R.string.FeeSelector_ethTime
                    else -> when (transferSpeed) {
                        is TransferSpeed.Economy -> R.string.FeeSelector_economyTime
                        is TransferSpeed.Regular -> R.string.FeeSelector_regularTime
                        is TransferSpeed.Priority -> R.string.FeeSelector_priorityTime
                    }
                }
            )
            processingTimeLabel.text =
                res.getString(R.string.Confirmation_processingTime, processingTime)

            transferFields.forEach { field ->
                when (field.key) {
                    TransferField.DESTINATION_TAG -> {
                        groupDestinationTag.isVisible = true
                        if (field.value.isNullOrEmpty()) {
                            destinationTagValue.setText(R.string.Confirmation_destinationTag_EmptyHint)
                        } else {
                            destinationTagValue.text = field.value
                        }
                    }
                    TransferField.HEDERA_MEMO -> {
                        groupHederaMemo.isVisible = true
                        if (field.value.isNullOrEmpty()) {
                            hederaMemoValue.setText(R.string.Confirmation_destinationTag_EmptyHint)
                        } else {
                            hederaMemoValue.text = field.value
                        }
                    }
                }
            }
        }
    }
}

data class ConfirmTradeModel(
    val currencyCode: String,
    val targetAddress: String,
    val transferSpeed: TransferSpeed,
    val amount: BigDecimal,
    val networkFee: BigDecimal,
    val transferFields: List<TransferField>
)
