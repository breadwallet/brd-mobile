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

class ExchangeFullScreenErrorView: UIView {

    enum Style {
        case emptyWallets
        case sellUnsupportedRegion
    }

    var style: Style = .emptyWallets {
        willSet { setup(for: newValue) }
    }

    var ctaAction: (() -> Void)? {
        didSet { ctaButton.tap = ctaAction }
    }

    private lazy var imageView = UIImageView(image: UIImage(named: "cryingEmoji"))

    private lazy var titleLabel = UILabel(
        text: S.Exchange.FullScreenErrorState.emptyWalletTitle,
        font: Theme.h1Title,
        color: Theme.primaryText
    )

    private lazy var bodyLabel = UILabel(
        text: S.Exchange.FullScreenErrorState.emptyWalletBody,
        font: Theme.body1,
        color: Theme.primaryText
    )

    private lazy var ctaButton: BRDButton = {
        let button = BRDButton(title: S.Exchange.FullScreenErrorState.emptyWalletCTA)
        button.tap = { [weak self] in self?.ctaAction?() }
        return button
    }()

    init() {
        super.init(frame: .zero)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    private func setupUI() {

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

    private func setup(for style: Style) {
        guard self.style != style else {
            return
        }

        switch style {
        case .emptyWallets:
            imageView.image = UIImage(named: "cryingEmoji")
            titleLabel.text = S.Exchange.FullScreenErrorState.emptyWalletTitle
            bodyLabel.text = S.Exchange.FullScreenErrorState.emptyWalletBody
            ctaButton.title = S.Exchange.FullScreenErrorState.emptyWalletCTA
        case .sellUnsupportedRegion:
            imageView.image = UIImage(named: "bank")
            titleLabel.text = S.Exchange.FullScreenErrorState.emptyWalletTitle
            bodyLabel.text = S.Exchange.FullScreenErrorState.emptyWalletBody
            ctaButton.title = S.Exchange.FullScreenErrorState.emptyWalletCTA
        }
    }
}

// MARK: - ExchangeBuySellViewViewModel

extension ExchangeFullScreenErrorView {

    func update(with viewModel: ExchangeBuySellViewModel) {
        guard let newStyle = viewModel.fullScreenErrorStyle else {
            isHidden = true
            return
        }
        isHidden = false
        style = newStyle
        ctaAction = viewModel.ctaAction
    }
}

// MARK: - ExchangeTradeViewModel

extension ExchangeFullScreenErrorView {

    func update(with viewModel: ExchangeTradeViewModel) {
        guard let newStyle = viewModel.fullScreenErrorStyle else {
            isHidden = true
            return
        }
        isHidden = false
        style = newStyle
        ctaAction = viewModel.nextAction
    }
}
