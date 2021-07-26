//
//  ExchangeTradePreviewFooter.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeTradePreviewFooter: UITableViewHeaderFooterView {

    let label = UILabel(font: Theme.caption, color: Theme.secondaryText)

    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

private extension ExchangeTradePreviewFooter {

    func setupUI() {
        let hPadding = C.padding[0]
        let vPadding = C.padding[2]
        let insets = UIEdgeInsets(forConstrains: hPadding, vVal: vPadding)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.numberOfLines = 0
        addSubview(label)
        label.constrain(toSuperviewEdges: insets)
    }
}
