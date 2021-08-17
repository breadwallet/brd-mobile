//
//  ExchangeOfferCell.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeOfferCell: UITableViewCell {

    private(set) var minMaxButton = BRDButton(title: " ")

    private var headerView = CellLayoutView()
    private var totalView = CellLayoutView()
    private var container = VStackView()
    private var bgView = UIView()

    override init(style: CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    func update(_ viewModel: ExchangeOfferViewModel.Offer?) {
        headerView.update(
            with: .init(
                title: viewModel?.title,
                subtitle: viewModel?.subtitle,
                // iconURL: viewModel?.iconURL,
                iconImage: viewModel?.iconImage
            )
        )

        setAccessoryHighlighted(viewModel?.selected ?? false)

        container.arrangedSubviews.forEach {
            [headerView].contains($0) ? () : $0.removeFromSuperview()
        }

        minMaxButton.isHidden = viewModel?.isValid ?? true
        minMaxButton.title = viewModel?.ctaTitle ?? ""

        guard viewModel?.isValid ?? false else {
            container.addArrangedSubview(minMaxButton)
            return
        }

        let rateView = infoView(
            title: S.Exchange.Offer.rate,
            detail: viewModel?.rate ?? ""
        )

        container.addArrangedSubview(rateView)
        container.addArrangedSubview(separatorView())

        if viewModel?.showFeesBreakDown ?? false {
            viewModel?.fees.forEach {
                let view = infoView(title: $0.title, detail: $0.amount)
                container.addArrangedSubview(view)
                container.addArrangedSubview(separatorView())
            }
        } else {
            let feeTotalView = infoView(
                title: S.Exchange.Offer.fees,
                detail: viewModel?.feesTotal ?? ""
            )
            container.addArrangedSubviews([feeTotalView, separatorView()])
        }

        totalView.update(
            with: .init(
                title: S.Exchange.Offer.total,
                rightTitle: viewModel?.totalQuote,
                rightSubtitle: viewModel?.totalBase
            )
        )

        container.addArrangedSubview(totalView)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupUI() {
        contentView.addSubview(bgView)
        contentView.addSubview(container)

        selectionStyle = .none

        let bgPadding = C.padding[2] + Padding.half
        let bgInsets = UIEdgeInsets(forConstrains: bgPadding, vVal: bgPadding)
        bgView.constrain(toSuperviewEdges: bgInsets)

        let padding = C.padding[4] + Padding.half
        let insets = UIEdgeInsets(forConstrains: padding, vVal: padding)
        container.constrain(toSuperviewEdges: insets)
        container.addArrangedSubview(headerView)
        container.spacing = C.padding[1]
        container.setCustomSpacing(C.padding[2] + Padding.half, after: headerView)

        backgroundColor = Theme.primaryBackground
        bgView.backgroundColor = Theme.quaternaryBackground
        bgView.layer.cornerRadius = C.padding[1]

        headerView.iconStyle = .circle(size: Constant.iconSize)
        headerView.titleStyle = .alwaysProminentTitle
        headerView.titleVStack.spacing = Padding.half

        totalView.rightTitleStyle = .alwaysProminentTitle
        totalView.titleLabel.font = Theme.body1Accent
        totalView.rightTitleLabel.font = Theme.body1Accent

        [totalView.rightTitleLabel, totalView.rightSubtitleLabel]
            .forEach { $0.textAlignment = .right }

        [headerView, totalView]
            .forEach { $0.backgroundColor = bgView.backgroundColor}
    }

    private func infoView(title: String, detail: String) -> UIView {
        let titleLabel = UILabel(font: Theme.body1, color: Theme.secondaryText)
        let detailLabel = UILabel(font: Theme.body1Accent, color: Theme.primaryText)
        detailLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        (titleLabel.text, detailLabel.text) = (title, detail)
        return HStackView([titleLabel, detailLabel])
    }

    private func separatorView() -> UIView {
        let separator = UIView()
        separator.addConstraint(separator.heightAnchor.constraint(equalToConstant: 1))
        separator.backgroundColor = Theme.tertiaryBackground
        return separator
    }

    private func setAccessoryHighlighted(_ highlighted: Bool) {
        guard #available(iOS 13.0, *) else {
            headerView.accessoryLabel.text = highlighted ? "✓" : ""
            return
        }

        let img = UIImage(named: "CircleCheckSolid") ?? UIImage()
        let imgAttachment = NSTextAttachment(image: img)
        let checkMark = NSAttributedString(attachment: imgAttachment)
        let blank = NSAttributedString(string: "")
        let attrString = highlighted ? checkMark : blank
        headerView.accessoryLabel.textColor = .brdGreen
        headerView.accessoryLabel.attributedText = attrString
    }
}

// MARK: - Constant

extension ExchangeOfferCell {

    enum Constant {
        static let iconSize = CGFloat(28)
    }
}
