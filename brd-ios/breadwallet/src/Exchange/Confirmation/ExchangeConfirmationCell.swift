//
//  ExchngeOrderCell.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeConfirmationCell: UITableViewCell {

    var title: String? {
        get { titleLabel.text }
        set { titleLabel.text = newValue }
    }

    var detail: String? {
        get { detailLabel.text }
        set { detailLabel.text = newValue }
    }

    var emphasizeDetail: Bool = false {
        didSet {
            let color = emphasizeDetail ? UIColor.gradientEnd : Theme.primaryText
            detailLabel.textColor = color
        }
    }

    var textColor: UIColor = Theme.primaryText {
        didSet {
            let contrast = textColor.contrastRatio(with: Theme.primaryBackground) > 2.0
            detailTextLabel?.textColor = contrast ? textColor : Theme.primaryText
        }
    }

    private lazy var titleLabel = UILabel(
        font: Theme.body1,
        color: Theme.tertiaryText
    )

    private lazy var detailLabel = UILabel(
        font: Theme.body1,
        color: Theme.primaryText
    )

    override init(style: CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    override func prepareForReuse() {
        super.prepareForReuse()
        emphasizeDetail = false
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupUI() {
        let container = HStackView([titleLabel, detailLabel])
        container.distribution = .fill
        contentView.addSubview(container)
        container.constrain(toSuperviewEdges: nil)
        titleLabel.constrain([
            titleLabel.widthAnchor.constraint(
                equalTo: contentView.widthAnchor,
                multiplier: 0.4
            )
        ])
    }
}
