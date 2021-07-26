//
//  ExchangeOrderFooterView.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeConfirmationFooterView: UITableViewHeaderFooterView {

    override func layoutSubviews() {
        super.layoutSubviews()
        configureUI()
    }

    private func configureUI() {
        textLabel?.numberOfLines = 0
        textLabel?.bounds.size = textLabel?.sizeThatFits(bounds.size) ?? .zero
        textLabel?.center = bounds.center
        textLabel?.font = Theme.caption
        textLabel?.textColor = Theme.secondaryText
        textLabel?.textAlignment = .left
    }
}
