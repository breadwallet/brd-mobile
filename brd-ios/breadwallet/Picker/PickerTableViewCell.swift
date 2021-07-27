// 
//  PickerTableViewCell.swift
//  breadwallet
//
//  Created by stringcode on 25/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

final class PickerTableViewCell: UITableViewCell {

    var tap: (() -> Void)?

    private(set) lazy var cellLayoutView = CellLayoutView()

    private lazy var cellBackgroundView = UIView()

    override init(style: CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        addSubViews()
        setupConstraints()
        setInitialData()
    }

    override func setHighlighted(_ highlighted: Bool, animated: Bool) {
        super.setHighlighted(highlighted, animated: animated)
        setHighlightedUI(highlighted, animated: animated)
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        setHighlightedUI(selected, animated: animated)
    }

    func update(with viewModel: PickerViewModel.Item) {
        cellLayoutView.update(
            with: .init(
                title: viewModel.title,
                subtitle: viewModel.subtitle,
                rightTitle: viewModel.rightTitle,
                rightSubtitle: viewModel.rightSubtitle,
                detail: viewModel.detail,
                iconImage: viewModel.iconImage,
                iconText: viewModel.iconText,
                iconURL: viewModel.iconURL
            )
        )
        setHighlightedUI(isSelected, animated: false)
        cellLayoutView.setNeedsLayout()
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        tap = nil
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func addSubViews() {
        contentView.addSubview(cellBackgroundView)
        contentView.addSubview(cellLayoutView)
    }

    private func setupConstraints() {
        let insets = UIEdgeInsets(
            forConstrains: C.padding[3],
            vVal: C.padding[2] + Padding.half
        )
        let bgInsets = UIEdgeInsets(forConstrains: C.padding[1], vVal: 0)
        cellLayoutView.constrain(toSuperviewEdges: insets)
        cellBackgroundView.constrain(toSuperviewEdges: bgInsets)
    }

    private func setInitialData() {
        selectionStyle = .none
        cellBackgroundView.layer.cornerRadius = C.padding[1]
        cellBackgroundView.clipsToBounds = true
        clipsToBounds = true
        contentView.clipsToBounds = true
        cellBackgroundView.backgroundColor = Theme.quaternaryBackground
        backgroundColor = Theme.primaryBackground
        cellLayoutView.backgroundColor = .clear
        cellLayoutView.contentStack.spacing = C.padding[1] + Padding.half
        cellLayoutView.addGestureRecognizer(
            UITapGestureRecognizer(
                target: self,
                action: #selector(tapAction(_:))
            )
        )
    }

    @objc private func tapAction(_ sender: Any?) {
        tap?()
    }

    private func setHighlightedUI(_ highlighted: Bool, animated: Bool) {
        let block = {
            self.setNeedsLayout()
            self.cellLayoutView.setNeedsLayout()
            self.setAccessoryHighlighted(highlighted)
            self.cellBackgroundView.isHidden = !highlighted
            self.layoutIfNeeded()
            self.cellLayoutView.layoutIfNeeded()
        }
        guard animated else  {
            block()
            return
        }
        
        UIView.animate(withDuration: C.animationDuration / 2, animations: block)
    }

    private func setAccessoryHighlighted(_ highlighted: Bool) {
        let image = UIImage(named: "ExchangeCheckmark")
        cellLayoutView.rightIconImageView.image = highlighted ? image : nil
    }
}
