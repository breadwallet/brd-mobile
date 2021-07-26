//
//  CurrencyInputButton.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class CurrencyInputButton: UIButton {

    private lazy var iconView = UIImageView()
    private lazy var currencyLabel = UILabel(font: Theme.body1Accent, color: Theme.primaryText)
    private lazy var chevronView = UIImageView(image: UIImage(named: "ChevronDown"))

    init() {
        super.init(frame: .zero)
        setupUI()
    }

    func update(with viewModel: ViewModel) {
        switch viewModel {
        case let .currency(icon, symbol):
            backgroundColor = .clear
            currencyLabel.text = symbol
            iconView.image = icon
            iconView.isHidden = false
        case let .select(title):
            backgroundColor = .blue
            currencyLabel.text = title
            iconView.isHidden = true
        }
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        if size.width < iconView.superview?.intrinsicContentSize.width ?? 0 {
            size.width = iconView.superview?.intrinsicContentSize.width ?? 0
        }
        return size
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - ViewModel

extension CurrencyInputButton {

    enum ViewModel {
        case currency(icon: UIImage?, symbol: String)
        case select(title: String)
    }
}

// MARK: - SetupUI

private extension CurrencyInputButton {

    func setupUI() {
        let views = [iconView, currencyLabel, chevronView]
        views.forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            $0.setContentCompressionResistancePriority(.required, for: .horizontal)
        }

        let stack = HStackView(views)
        stack.alignment = .center
        stack.spacing = C.padding[1]
        addSubview(stack)
        stack.constrain(toSuperviewEdges: UIEdgeInsets(forConstrains: C.padding[1]))
        stack.isUserInteractionEnabled = false
        stack.setContentCompressionResistancePriority(.required, for: .horizontal)

        iconView.backgroundColor = Theme.primaryText.withAlphaComponent(0.25)
        iconView.layer.cornerRadius = Padding.half
        iconView.tintColor = Theme.primaryText
        iconView.constrain([
            iconView.widthAnchor.constraint(equalToConstant: Constant.iconSize),
            iconView.heightAnchor.constraint(equalToConstant: Constant.iconSize)
        ])

        currencyLabel.textAlignment = .center
        currencyLabel.font = Theme.body1Accent
        chevronView.alpha = 0.4
        layer.cornerRadius = Padding.half
        setContentCompressionResistancePriority(.required, for: .horizontal)
    }
}

// MARK: - Constant

private extension CurrencyInputButton {

    enum Constant {
        static let iconSize: CGFloat = 36
    }
}
