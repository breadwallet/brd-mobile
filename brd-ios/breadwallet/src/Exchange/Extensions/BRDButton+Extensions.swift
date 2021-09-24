//
//  BRDButton+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension BRDButton {

    func update(with viewModel: ExchangeBuySellViewModel.CTAState) {
        title = viewModel.text()
        setActivityViewVisible(viewModel == .processing)
        isEnabled = viewModel.isEnabled()
    }

    func update(with viewModel: ExchangeTradeViewModel.CTAState) {
        title = viewModel.text()
        setActivityViewVisible(viewModel == .processing)
        isEnabled = viewModel.isEnabled()
    }

    func update(with viewModel: ExchangeTradePreviewViewModel.CTAState) {
        title = viewModel.text()
        setActivityViewVisible(viewModel == .processing)
        isEnabled = viewModel.isEnabled()
    }
}
