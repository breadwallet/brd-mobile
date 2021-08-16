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

class ExchangeTradeEmptyWalletView: UIView {

    var ctaAction: (() -> Void)?

    init() {
        super.init(frame: .zero)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    private func setupUI() {
        let imageView = UIImageView(image: UIImage(named: "ghost"))

        let titleLabel = UILabel(
            text: S.Exchange.emptyWalletTitle,
            font: Theme.h1Title,
            color: Theme.primaryText
        )

        let bodyLabel = UILabel(
            text: S.Exchange.emptyWalletBody,
            font: Theme.body1,
            color: Theme.primaryText
        )

        let ctaButton = BRDButton(title: S.Exchange.emptyWalletCTA)
        ctaButton.tap = { [weak self] in
            self?.ctaAction?()
        }

        [titleLabel, bodyLabel].forEach {
            $0.numberOfLines = 0
            $0.textAlignment = .center
        }

        let stack = VStackView([imageView, titleLabel, bodyLabel, ctaButton])
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.alignment = .center
        stack.distribution = .fill
        addSubview(stack)

        stack.constrain([
            stack.centerXAnchor.constraint(equalTo: centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: centerYAnchor),
            stack.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor),
            stack.leadingAnchor.constraint(greaterThanOrEqualTo: leadingAnchor),
            stack.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor),
            ctaButton.leadingAnchor.constraint(equalTo: leadingAnchor),
            ctaButton.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])

        stack.setCustomSpacing(C.padding[2] + Padding.half, after: imageView)
        stack.setCustomSpacing(C.padding[2] + Padding.half, after: titleLabel)
        stack.setCustomSpacing(C.padding[4] + Padding.half, after: bodyLabel)
    }
}
