//
//  CurrencyInputViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

struct CurrencyInputViewModel {

    // Data
    let text: String?
    let detail: String?
    let icon: UIImage?
    let symbol: String?
    let bgColor: (UIColor?, UIColor?)
    let inputEnabled: Bool
    let isLoading: Bool

    // Actions
    let didChangeAction: ((_ old: String, _ new: String) -> Void)?
    let didEndEditingAction: ((String) -> Void)?
    let clearAction: ((String) -> Void)?
    let minAction: (() -> Void)?
    let maxAction: (() -> Void)?
    let currencyAction: (() -> Void)?
}

extension CurrencyInputViewModel {

    static func empty(
        _ isLoading: Bool = true,
        _ action: (() -> Void)? = nil
    ) -> CurrencyInputViewModel {
        return .init(
            text: nil,
            detail: nil,
            icon: nil,
            symbol: nil,
            bgColor: (nil, nil),
            inputEnabled: true,
            isLoading: isLoading,
            didChangeAction: nil,
            didEndEditingAction: nil,
            clearAction: nil,
            minAction: nil,
            maxAction: nil,
            currencyAction: action
        )
    }
}
