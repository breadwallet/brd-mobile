//
//  ExchangeTradePreviewHeader.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeTradePreviewHeader: UITableViewHeaderFooterView {

    let fromIcon = UIImageView()
    let fromLabel = UILabel(font: Theme.body1, color: Theme.primaryText)
    let toIcon = UIImageView()
    let toLabel = UILabel(font: Theme.body1, color: Theme.primaryText)
    let contentStack = HStackView()
    let contentContainer = UIView()

    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let inset = C.padding[4] + Padding.half
        superview?.clipsToBounds = false
        contentContainer.frame = bounds.insetBy(dx: -inset, dy: 0)
        contentStack.setNeedsLayout()
        contentStack.layoutIfNeeded()

        [fromIcon, toIcon]
            .compactMap { $0.superview }
            .forEach { $0.layer.cornerRadius = $0.bounds.width / 2 }
    }

    func update(with viewModel: ExchangeTradePreviewHeaderViewModel?) {
        (fromIcon.superview as? GradientWrapperView)?.colors = viewModel?.fromColors ?? []
        (toIcon.superview as? GradientWrapperView)?.colors = viewModel?.toColors ?? []
        fromIcon.image = viewModel?.fromIcon
        toIcon.image = viewModel?.toIcon
        fromLabel.text = viewModel?.fromSymbol
        toLabel.text = viewModel?.toSymbol
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

private extension ExchangeTradePreviewHeader {

    func setupUI() {
        let (fromContainer, toContainer) = (GradientWrapperView(), GradientWrapperView())
        let fromStack = VStackView([fromContainer, fromLabel], alignment: .center)
        let toStack = VStackView([toContainer, toLabel], alignment: .center)
        let arrow = UIImageView(image: UIImage(named: "ArrowRight"))
        let arrowSpacer = UIView()
        let arrowStack = VStackView([arrowSpacer, arrow, UIView()])
        let views = [UIView(), fromStack, arrowStack, toStack, UIView()]

        contentStack.addArrangedSubviews(views)
        contentStack.alignment = .center
        contentStack.spacing = C.padding[3]
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        fromContainer.addSubview(fromIcon)
        toContainer.addSubview(toIcon)

        [self, contentView, contentContainer]
            .forEach { $0.clipsToBounds = false }

        [fromIcon, toIcon].forEach {
            $0.contentMode = .scaleAspectFit
            $0.clipsToBounds = true
            $0.backgroundColor = .clear
            $0.tintColor = .white
            let padding = C.padding[1] + Padding.half
            $0.constrain(toSuperviewEdges: UIEdgeInsets(forConstrains: padding))
        }

        [fromStack, toStack].forEach {
            $0.spacing = C.padding[2]
        }

        [fromContainer, toContainer].forEach {
            $0.backgroundColor = Theme.quaternaryBackground
            $0.backgroundColor = .brdRed
            $0.clipsToBounds = true
        }

        contentView.addSubview(contentContainer)
        contentContainer.addSubview(contentStack)
        contentContainer.backgroundColor = Theme.quaternaryBackground

        contentView.constrain([
            fromContainer.widthAnchor.constraint(equalToConstant: Constant.iconSize),
            fromContainer.heightAnchor.constraint(equalToConstant: Constant.iconSize),
            toContainer.widthAnchor.constraint(equalToConstant: Constant.iconSize),
            toContainer.heightAnchor.constraint(equalToConstant: Constant.iconSize),
            arrowStack.heightAnchor.constraint(equalTo: contentStack.heightAnchor),
            arrowSpacer.heightAnchor.constraint(equalTo: fromIcon.heightAnchor, multiplier: 0.5),
            contentStack.centerXAnchor.constraint(equalTo: contentContainer.centerXAnchor),
            contentStack.centerYAnchor.constraint(equalTo: contentContainer.centerYAnchor)
        ])
    }
}

private extension ExchangeTradePreviewHeader {

    enum Constant {
        static let iconSize: CGFloat = 68
    }
}

// MARK: - ExchangeTradePreviewHeaderViewModel

struct ExchangeTradePreviewHeaderViewModel {
    let fromColors: [UIColor?]
    let toColors: [UIColor?]
    let fromIcon: UIImage?
    let toIcon: UIImage?
    let fromSymbol: String
    let toSymbol: String
}
