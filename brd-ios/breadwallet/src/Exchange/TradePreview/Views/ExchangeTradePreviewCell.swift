//
//  ExchangeTradePreviewCell.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeTradePreviewCell: UITableViewCell {

    var topAlightTitle = false {
        didSet {
            titleSpacerTop.isHidden = !topAlightTitle
            titleSpacerBottom.isHidden = !topAlightTitle
        }
    }

    private let titleLabel = UILabel()
    private let titleIcon = UIImageView(image: UIImage(named: "ArrowSmallDown"))
    private let cellLayoutView = CellLayoutView()
    private let titleSpacerTop = UIView()
    private let titleSpacerBottom = UIView()

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    func update(with viewModel: ExchangeTradePreviewViewModel.InfoItem, expanded: Bool) {
        titleLabel.textColor = Theme.secondaryText
        cellLayoutView.titleLabel.textColor = Theme.primaryText
        cellLayoutView.titleStyle = .alwaysProminentTitle
        titleIcon.image = nil
        switch viewModel {
        case let .simple(key, value, color, bold):
            titleLabel.text = key
            cellLayoutView.update(with: .init(title: value ?? cellLayoutView.titleLabel.text))
            cellLayoutView.titleLabel.textColor = textColor(for: color)
            cellLayoutView.titleLabel.font = bold ? Theme.body1Accent : Theme.body1
        case let .feeTotal(value, detail, color):
            titleLabel.text = S.Exchange.Order.fees
            titleIcon.image = UIImage(named: expanded ? "ArrowSmallUp" : "ArrowSmallDown")
            cellLayoutView.update(
                with: .init(
                    title: expanded ? "" : value,
                    subtitle: expanded ? "" : detail
                )
            )
            cellLayoutView.subtitleLabel.textColor = color
        case let .fee(key, value, detail, detailColor):
            titleLabel.text = key
            cellLayoutView.update(
                with: .init(
                    title: value ?? cellLayoutView.titleLabel.text,
                    subtitle: detail ?? cellLayoutView.subtitleLabel.text
                )
            )
            cellLayoutView.subtitleLabel.textColor = detailColor
        case let .reward(active):
            titleLabel.text = S.Exchange.Preview.rewards
            let title = active ? S.Exchange.Preview.active : S.Exchange.Preview.inactive
            let subtitle = active ? nil : S.Exchange.Preview.info
            let colors = [UIColor.newGradientStart, UIColor.newGradientEnd]
            let image = UIImage.gradient(colors, size: Constant.gradientSize, vertical: true)
            titleLabel.textColor = UIColor(patternImage: image ?? UIImage())
            cellLayoutView.update(with: .init(title: title, subtitle: subtitle))
            cellLayoutView.titleLabel.textColor = active ? .brdGreen : .failedRed
            cellLayoutView.titleVStack.spacing = C.padding[1]
            cellLayoutView.subtitleLabel.textColor = Theme.secondaryText
            cellLayoutView.subtitleLabel.numberOfLines = 0
        }
    }

    override func prepareForReuse() {
        super.prepareForReuse()
        cellLayoutView.titleLabel.font = Theme.body1
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - UI setup

private extension ExchangeTradePreviewCell {

    func setupUI() {
        let stack = HStackView(
            [
                 VStackView([titleSpacerTop, titleLabel, titleSpacerBottom]),
                 titleIcon,
                 UIView(),
                 cellLayoutView
            ]
        )
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.spacing = C.padding[1]
        contentView.addSubview(stack)
        addConstraints([
            stack.topAnchor.constraint(equalTo: topAnchor),
            stack.bottomAnchor.constraint(equalTo: bottomAnchor),
            stack.leadingAnchor.constraint(equalTo: leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: trailingAnchor),
            titleSpacerTop.heightAnchor.constraint(equalToConstant: C.padding[1]),
            cellLayoutView.widthAnchor.constraint(
                equalTo: widthAnchor,
                multiplier: Constant.titleContainerRatio
            )
        ])
        titleLabel.font = Theme.body2
        titleLabel.textColor = Theme.secondaryText
        titleIcon.contentMode = .center
        cellLayoutView.titleStyle = .alwaysProminentTitle
        selectionStyle = .none
    }

    func textColor(for color: UIColor) -> UIColor {
        let contrast = color.contrastRatio(with: Theme.primaryBackground) > 2.0
        return contrast ? color : Theme.primaryText
    }

    enum Constant {
        static let titleContainerRatio = CGFloat(0.58)
        static let gradientSize = CGSize(width: 85, height: 30)
    }
}
