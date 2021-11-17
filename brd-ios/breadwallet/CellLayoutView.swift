//
//  ItemContentView.swift
//  breadwallet
// 
//  Created by blockexplorer on 08/04/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

final class CellLayoutView: UIView {

    private(set) lazy var titleLabel = UILabel()
    private(set) lazy var subtitleLabel = UILabel()
    private(set) lazy var rightTitleLabel = UILabel()
    private(set) lazy var rightSubtitleLabel = UILabel()
    private(set) lazy var accessoryLabel = UILabel()
    private(set) lazy var iconImageView = UIImageView(image: nil)
    private(set) lazy var iconLabel = UILabel()
    private(set) lazy var titleVStack = VStackView([titleLabel, subtitleLabel])
    private(set) lazy var rightIconImageView = UIImageView(image: nil)
    private(set) lazy var rightTitleVStack = VStackView(
        [rightTitleLabel, rightSubtitleLabel]
    )
    private(set) lazy var contentStack = HStackView(
        [iconStack, titleVStack, UIView(),rightTitleVStack, accessoryLabel, rightIconImageView]
    )

    private lazy var iconStack = HStackView([iconImageView, iconLabel])

    private var iconWidthConstraint: NSLayoutConstraint?
    private var iconHeightConstraint: NSLayoutConstraint?

    var iconStyle: IconStyle = .default {
        didSet { update(for: iconStyle) }
    }

    var titleStyle: LabelLayoutStyle = .default {
        didSet { update(for: titleStyle) }
    }

    var rightTitleStyle: LabelLayoutStyle = .default {
        didSet { update(for: rightTitleStyle, right: true) }
    }

    var accessoryStyle: AccessoryStyle = .default {
        didSet { update(for: accessoryStyle) }
    }

    init() {
        super.init(frame: .zero)
        addSubViews()
        setupConstraints()
        setupInitialData()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        updateHiddenBasedOnContent()
        if titleStyle == .prominentOnEmptySubtitle {
            update(for: titleStyle)
        }
        if rightTitleStyle == .prominentOnEmptySubtitle {
            update(for: rightTitleStyle, right: true)
        }
    }

    func update(with viewModel: CellLayoutView.ViewModel) {
        titleLabel.text = viewModel.title
        subtitleLabel.text = viewModel.subtitle
        subtitleLabel.textColor = viewModel.subtitleColor ?? Theme.tertiaryText
        rightTitleLabel.text = viewModel.rightTitle
        rightSubtitleLabel.text = viewModel.rightSubtitle
        accessoryLabel.text = viewModel.detail
        iconImageView.image = viewModel.iconImage
        rightIconImageView.image = viewModel.rightIconImage
        iconLabel.text = viewModel.iconText
        if let iconURL = viewModel.iconURL {
            iconImageView.setImage(url: iconURL, placeholder: .activityIndicator)
        }
        updateHiddenBasedOnContent()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func addSubViews() {
        addSubview(contentStack)
    }

    private func setupConstraints() {
        contentStack.constrain(toSuperviewEdges: nil)
        contentStack.spacing = C.padding[1]
        contentStack.constrain([
            contentStack.heightAnchor.constraint(greaterThanOrEqualToConstant: 44)
        ])
        iconWidthConstraint = iconStack.widthAnchor.constraint(
            equalToConstant: Constant.iconLength
        )
        iconHeightConstraint = iconStack.heightAnchor.constraint(
            equalToConstant: Constant.iconLength
        )
        iconStack.constrain(
            [iconWidthConstraint, iconHeightConstraint].compactMap { $0 }
        )
        [accessoryLabel, iconImageView, rightIconImageView].forEach {
            $0.setContentHuggingPriority(.required, for: .horizontal)
        }
        titleVStack.distribution = .fillProportionally
        contentStack.spacing = C.padding[1]
    }

    private func setupInitialData() {
        backgroundColor = Theme.primaryBackground
        contentStack.alignment = .center
        iconLabel.textAlignment = .center
        iconLabel.font = Theme.h0Title
        iconLabel.minimumScaleFactor = 0.5
        iconLabel.adjustsFontSizeToFitWidth = true
        [titleLabel, subtitleLabel, rightTitleLabel, rightSubtitleLabel, accessoryLabel]
            .forEach {
                $0.textColor = Theme.primaryText
                $0.font = Theme.body1
        }
    }

    private func updateHiddenBasedOnContent() {
        titleLabel.isHidden = titleLabel.text?.isEmpty ?? true
        subtitleLabel.isHidden = subtitleLabel.text?.isEmpty ?? true
        rightTitleLabel.isHidden = rightTitleLabel.text?.isEmpty ?? true
        rightSubtitleLabel.isHidden = rightSubtitleLabel.text?.isEmpty ?? true
        accessoryLabel.isHidden = accessoryLabel.text?.isEmpty ?? true && accessoryLabel.attributedText?.string.isEmpty ?? true
        iconImageView.isHidden = iconImageView.image == nil
        iconLabel.isHidden = iconLabel.text == nil
        let hideIcon = iconStyle.isDefault && iconImageView.image == nil && iconLabel.text == nil
        iconStack.isHidden = hideIcon
        rightIconImageView.isHidden = rightIconImageView.image == nil
    }
}

// MARK: - ViewModel

extension  CellLayoutView {

    struct ViewModel {
        let title: String?
        let subtitle: String?
        let rightTitle: String?
        let rightSubtitle: String?
        let detail: String?
        let subtitleColor: UIColor?
        let iconImage: UIImage?
        let iconText: String?
        let iconURL: URL?
        let rightIconImage: UIImage?

        init(
            title: String? = nil,
            subtitle: String? = nil,
            rightTitle: String? = nil,
            rightSubtitle: String? = nil,
            detail: String? = nil,
            subtitleColor: UIColor? = nil,
            iconImage: UIImage? = nil,
            iconText: String? = nil,
            iconURL: URL? = nil,
            rightIconImage: UIImage? = nil
        ) {
            self.title = title
            self.subtitle = subtitle
            self.rightTitle = rightTitle
            self.rightSubtitle = rightSubtitle
            self.detail = detail
            self.subtitleColor = subtitleColor
            self.iconImage = iconImage
            self.iconText = iconText
            self.iconURL = iconURL
            self.rightIconImage = rightIconImage
        }
    }
}

// MARK: - Style handling

extension CellLayoutView {

    enum LabelLayoutStyle {
        case `default`
        case alwaysProminentTitle
        case prominentOnEmptySubtitle
    }

    enum IconStyle {
        case `default`
        case circle(size: CGFloat)
        case square(size: CGFloat)

        var isDefault: Bool {
            switch self {
            case .default:
                return true
            default:
                return false
            }
        }
    }

    enum AccessoryStyle {
        case `default`
        case large
    }

    private func update(for style: LabelLayoutStyle, right: Bool = false) {
        let titleLabel = right ? rightTitleLabel : self.titleLabel
        let subtitleLabel = right ? rightSubtitleLabel : self.subtitleLabel

        switch style {
        case .default:
            setupInitialData()
        case .alwaysProminentTitle:
            titleLabel.textColor = Theme.primaryText
            titleLabel.font = Theme.body1
            subtitleLabel.textColor = Theme.tertiaryText
            subtitleLabel.font = Theme.caption
        case .prominentOnEmptySubtitle:
            let isEmpty = subtitleLabel.text?.isEmpty ?? false
            titleLabel.font = isEmpty ? Theme.body1 : Theme.caption
            titleLabel.textColor = isEmpty ? Theme.primaryText : Theme.secondaryText
        }
    }

    private func update(for style: IconStyle) {
        switch style {
        case .default:
            iconWidthConstraint?.constant = Constant.iconLength
            iconHeightConstraint?.constant = Constant.iconLength
            iconStack.layer.cornerRadius = 0
        case let .circle(size):
            iconWidthConstraint?.constant = size
            iconHeightConstraint?.constant = size
            iconStack.layer.cornerRadius = size / 2
            iconStack.clipsToBounds = true
        case let .square(size):
            iconWidthConstraint?.constant = size
            iconHeightConstraint?.constant = size
            iconStack.layer.cornerRadius = 0
            iconStack.clipsToBounds = true
        }
    }

    private func update(for style: AccessoryStyle) {
        accessoryLabel.font = style == .large ? Theme.h3Title : Theme.body1
    }
}

// MARK: - Constant

private extension CellLayoutView {

    enum Constant {
        static let iconLength: CGFloat = 44
    }
}
