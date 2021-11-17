//
//  ItemContentButton.swift
//  breadwallet
//
//  Created by blockexplorer on 08/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//	See the LICENSE file at the project root for license information.
//
	
import UIKit

class CellLayoutButton: UIButton {

    var title: String? {
        get { cellLayoutView.titleLabel.text }
        set {
            cellLayoutView.titleLabel.text = newValue
            setNeedsLayout()
        }
    }

    var subtitle: String? {
        get { cellLayoutView.subtitleLabel.text }
        set {
            cellLayoutView.subtitleLabel.text = newValue
            setNeedsLayout()
        }
    }

    var details: String? {
        get { cellLayoutView.accessoryLabel.text }
        set {
            cellLayoutView.accessoryLabel.text = newValue
            setNeedsLayout()
        }
    }

    var icon: UIImage? {
        get { cellLayoutView.iconImageView.image }
        set {
            cellLayoutView.iconImageView.image = newValue
            cellLayoutView.iconLabel.text = nil
            setNeedsLayout()
        }
    }

    var iconURL: URL? {
        get { return  nil }
        set {
            cellLayoutView.iconImageView.setImage(url: newValue, placeholder: .activityIndicator)
            cellLayoutView.iconLabel.text = nil
            setNeedsLayout()
        }
    }

    var iconText: String? {
        get { return  cellLayoutView.iconLabel.text }
        set {
            cellLayoutView.iconLabel.text = newValue
            cellLayoutView.iconImageView.cancelImageLoad()
            cellLayoutView.iconImageView.image = nil
            setNeedsLayout()
        }
    }

    var titleStyle: CellLayoutView.LabelLayoutStyle {
        get { cellLayoutView.titleStyle }
        set { cellLayoutView.titleStyle = newValue }
    }

    var iconStyle: CellLayoutView.IconStyle {
        get { cellLayoutView.iconStyle }
        set { cellLayoutView.iconStyle = newValue }
    }

    var accessoryStyle: CellLayoutView.AccessoryStyle {
        get { cellLayoutView.accessoryStyle }
        set { cellLayoutView.accessoryStyle = newValue }
    }

    let cellLayoutView = CellLayoutView()

    convenience init(
        title: String? = nil,
        subtitle: String? = nil,
        detail: String? = nil,
        image: UIImage? = nil,
        titleStyle: CellLayoutView.LabelLayoutStyle = .default,
        accessoryStyle: CellLayoutView.AccessoryStyle = .default
    ) {
        self.init(type: .custom)
        addSubview(cellLayoutView)

        let insets = UIEdgeInsets(
            forConstrains: C.padding[1] + Padding.half,
            vVal: C.padding[1]
        )

        cellLayoutView.constrain(toSuperviewEdges: insets)
        cellLayoutView.isUserInteractionEnabled = false
        cellLayoutView.titleStyle = titleStyle
        cellLayoutView.accessoryStyle = accessoryStyle
        cellLayoutView.subtitleLabel.textColor = Theme.tertiaryText

        let viewModel = CellLayoutView.ViewModel(
            title: title,
            subtitle: subtitle,
            detail: detail,
            iconImage: image
        )

        update(with: viewModel)
        backgroundColor = Theme.primaryBackground
        cellLayoutView.backgroundColor = backgroundColor
        cellLayoutView.titleVStack.spacing = Padding.half
        layer.cornerRadius = C.padding[1]
    }

    func update(with viewModel: CellLayoutView.ViewModel) {
        cellLayoutView.update(with: viewModel)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        cellLayoutView.setNeedsLayout()
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        size.height = Constant.height
        return size
    }

    func showIconSpinner() {
        cellLayoutView.iconImageView.addActivityIndicator()
        cellLayoutView.setNeedsLayout()
    }

    func hideIconSpinner() {
        cellLayoutView.iconImageView.removeActivityIndicator()
        cellLayoutView.setNeedsLayout()
    }
}

// MARK: - Constant

private extension CellLayoutButton {

    enum Constant {
        static let height: CGFloat = 54
    }
}
