//
//  SegmentPickerView.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 8/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class SegmentPickerView: UIView {

    var selectedIndex: Int? {
        didSet { setNeedsUpdateUI() }
    }

    var items: [String] = [] {
        didSet { setNeedsUpdateUI() }
    }

    var font: UIFont = Theme.body1 {
        didSet { setNeedsUpdateUI() }
    }

    var textColor: UIColor = Theme.secondaryText {
        didSet { setNeedsUpdateUI() }
    }

    var itemBackgroundColor: UIColor = Theme.primaryBackground {
        didSet { setNeedsUpdateUI() }
    }

    var selectedColor: UIColor = Theme.accent {
        didSet { setNeedsUpdateUI() }
    }

    var preferredItemHeight: CGFloat? {
        didSet { invalidateIntrinsicContentSize() }
    }

    var onTapAction: ((_ selectedIndex: Int) -> Void)?

    private var needsUpdateUI: Bool = false

    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .fillEqually
        stackView.spacing = 4
        return stackView
    }()

    override func layoutSubviews() {
        if needsUpdateUI {
            updateUI()
            needsUpdateUI = false
        }
        super.layoutSubviews()
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        size.height = Constant.defaultHeight
        return size
    }

    private func updateUI() {
        setupStackIfNeeded()

        for (idx, item) in items.enumerated() {
            let label: UILabel?

            if idx < stackView.arrangedSubviews.count {
                label = stackView.arrangedSubviews[idx] as? UILabel
            } else {
                let newLabel = UILabel()
                label = newLabel
                label?.textAlignment = .center
                label?.layer.cornerRadius = 6
                label?.clipsToBounds = true
                stackView.addArrangedSubview(newLabel)
            }

            let selected = selectedIndex == idx
            label?.font = font
            label?.textColor = selected ? Theme.primaryText : textColor
            label?.backgroundColor = selected ? selectedColor : itemBackgroundColor
            label?.text = item
        }

        while items.count > stackView.arrangedSubviews.count {
            let view = stackView.arrangedSubviews[stackView.arrangedSubviews.count - 1]
            stackView.removeArrangedSubview(view)
        }
    }

    private func setNeedsUpdateUI() {
        needsUpdateUI = true
        setNeedsLayout()
    }

    private func setupStackIfNeeded() {
        guard stackView.superview == nil else {
            return
        }

        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)

        stackView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        stackView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        stackView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true

        let tap = UITapGestureRecognizer(target: self, action: #selector(tapAction(_:)))
        addGestureRecognizer(tap)
    }

    @objc private func tapAction(_ recognizer: UITapGestureRecognizer) {
        for (idx, view) in stackView.arrangedSubviews.enumerated() {
            let touchPoint = recognizer.location(in: self)
            if view.convert(view.bounds, to: self).contains(touchPoint) {
                selectedIndex = idx
            }
        }

        if let selectedIndex = self.selectedIndex {
            onTapAction?(selectedIndex)
        }
    }

    enum Constant {
        static let defaultHeight: CGFloat = 33
    }
}

// MARK: - ExchangeBuySellViewModel

extension SegmentPickerView {

    func update(with viewModel: ExchangeBuySellViewModel) {
        items = viewModel.inputPresets
        selectedIndex = viewModel.selectedInputPreset
        onTapAction = viewModel.inputPresetAction
        alpha = items.isEmpty ? 0 : 1
    }
}