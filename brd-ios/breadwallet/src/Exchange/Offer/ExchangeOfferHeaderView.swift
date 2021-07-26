//
//  ExchangeOfferHearder.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeOfferHeaderView: UITableViewHeaderFooterView {

    override func layoutSubviews() {
        super.layoutSubviews()
        configureUI()
    }

    private func configureUI() {
        textLabel?.numberOfLines = 0
        textLabel?.sizeToFit()
        textLabel?.bounds.size.width = contentView.bounds.width - C.padding[5]
        textLabel?.center.x = bounds.midX
        textLabel?.font = Theme.body2
        textLabel?.textColor = Theme.primaryText
        textLabel?.backgroundColor = .clear
        textLabel?.textAlignment = .left
    }
}
