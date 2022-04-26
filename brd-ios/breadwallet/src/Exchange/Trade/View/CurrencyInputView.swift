//
//  ExchangeViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class CurrencyInputView: UIView {

    var didChangeAction: ((_ old: String, _ new: String) -> Void)?
    var didEndEditingAction: ((String) -> Void)?
    var clearAction: ((String) -> Void)?
    var minAction: (() -> Void)?
    var maxAction: (() -> Void)?
    var currencyAction: (() -> Void)?

    private lazy var gradientLayer = CAGradientLayer()
    private lazy var textField = UITextField()
    private lazy var detailLabel =  UILabel(color: Theme.tertiaryBackground)
    private lazy var currencyButton = CurrencyInputButton()
    private lazy var previousText = ""
    private lazy var textStack = VStackView([textField, detailLabel])
    private lazy var stack = HStackView([textStack, currencyButton])
    private lazy var blurView = UIVisualEffectView(effect: UIBlurEffect(style: .regular))

    init() {
        super.init(frame: .zero)
        setupUI()
        setupActions()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func update(with viewModel: CurrencyInputViewModel) {
        previousText = viewModel.text ?? ""
        textField.text = viewModel.text ?? ""
        textField.isUserInteractionEnabled = viewModel.inputEnabled
        detailLabel.text = viewModel.detail
        backgroundColor = viewModel.bgColor.0 ?? Theme.tertiaryBackground
        didChangeAction = viewModel.didChangeAction
        didEndEditingAction = viewModel.didEndEditingAction
        clearAction = viewModel.clearAction
        minAction = viewModel.minAction
        maxAction = viewModel.maxAction
        currencyAction = viewModel.currencyAction
        detailLabel.isHidden = viewModel.detail?.isEmpty ?? true

        if let symbol = viewModel.symbol {
            let icon = viewModel.icon
            currencyButton.update(with: .currency(icon: icon, symbol: symbol))
        } else {
            currencyButton.update(with: .select(title: " " + S.Exchange.selectAsset))
        }

        gradientLayer.colors = [viewModel.bgColor.0, viewModel.bgColor.1]
            .compactMap { $0 }
            .map { $0.cgColor }

        animateStateTransitionIfNeeded(viewModel)
    }

    func shakeAnimate() {
        layer.add(ShakeNoAnimation.animation(), forKey: "position.x")
    }

    func animateStateTransitionIfNeeded(_ viewModel: CurrencyInputViewModel) {
        UIView.animate(
            withDuration: C.animationDuration / 4,
            animations: {
                self.stack.alpha = viewModel.isLoading ? 0 : 1
            },
            completion: { _ in
                self.blurView.isHidden = !viewModel.isLoading
                self.gradientLayer.isHidden = viewModel.isLoading
                self.stack.isHidden = viewModel.isLoading
            }
        )

        gradientLayer.opacity = viewModel.isLoading ? 0 : 1
        let animation = CABasicAnimation(keyPath: "opacity")
        animation.fromValue = viewModel.isLoading ? 1 : 0
        animation.toValue = viewModel.isLoading ? 0 : 1
        animation.duration = C.animationDuration / 4
        gradientLayer.add(animation, forKey: "opacity")
    }

    func isEditing() -> Bool {
        return textField.isFirstResponder
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.bounds = bounds
        gradientLayer.position = bounds.center
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        size.height = Constant.defaultHeight
        return size
    }
}

// MARK: - UITextFieldDelegate

extension  CurrencyInputView: UITextFieldDelegate {

    @objc func valueChangedAction(_ textField: UITextField) {
        didChangeAction?(previousText, textField.text ?? "")
    }

    func textFieldDidEndEditing(_ textField: UITextField) {
        didEndEditingAction?(textField.text ?? "")
    }

    func textFieldShouldClear(_ textField: UITextField) -> Bool {
        clearAction?(textField.text ?? "")
        return true
    }
}

// MARK: - UI Setup

private extension CurrencyInputView {

    func setupUI() {
        addSubview(blurView)
        layer.addSublayer(gradientLayer)
        addSubview(stack)

        let hPadding = C.padding[1] + Padding.half
        let vInset = C.padding[2]
        let insets = UIEdgeInsets(
            top: vInset,
            left: hPadding,
            bottom: -vInset,
            right: -C.padding[1]
        )

        blurView.constrain(toSuperviewEdges: nil)
        stack.constrain(toSuperviewEdges: insets)
        stack.alignment = .center
        stack.distribution = .fill

        textStack.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        currencyButton.setContentCompressionResistancePriority(.required, for: .horizontal)
        currencyButton.setContentHuggingPriority(.required, for: .horizontal)
        currencyButton.update(with: .select(title: "  Select Asset"))

        textField.font = Theme.h1TitleAccent
        textField.textColor = Theme.primaryText
        textField.tintColor = textField.textColor
        textField.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        textField.adjustsFontSizeToFitWidth = true
        textField.minimumFontSize = Theme.body2.pointSize
        textField.attributedPlaceholder = NSAttributedString(
            string: "0",
            attributes: [
                .foregroundColor: Theme.tertiaryText,
                .font: textField.font ?? UIFont.header
            ]
        )

        detailLabel.font = Theme.caption
        detailLabel.textColor = Theme.secondaryText
        detailLabel.backgroundColor = .clear
        textField.keyboardType = .decimalPad
        textField.keyboardAppearance = .dark

        layer.cornerRadius = Padding.half
        clipsToBounds = true
        backgroundColor = UIColor.white.withAlphaComponent(0.1)
        gradientLayer.cornerRadius = layer.cornerRadius
        gradientLayer.startPoint = CGPoint(x: 0, y: 0.5)
        gradientLayer.endPoint = CGPoint(x: 1, y: 0.5)

        blurView.alpha = 0.5
        gradientLayer.opacity = 0
        stack.alpha = 0
    }

    func setupActions() {
        let spacer = UIBarButtonItem(.flexibleSpace)
        let minButton = UIBarButtonItem("Min", onTap: { [weak self] in
            self?.minAction?()
        })

        let maxButton = UIBarButtonItem("Max", onTap: { [weak self] in
            self?.maxAction?()
        })

        let doneButton = UIBarButtonItem(.done, onTap: { [weak self] in
            self?.textField.resignFirstResponder()
        })

        let size = CGSize(width: C.Sizes.barHeight, height: C.Sizes.barHeight)
        let toolbar = UIToolbar(frame: CGRect(origin: .zero, size: size))
        toolbar.setItems([maxButton, spacer, doneButton], animated: false)
        [minButton, maxButton].forEach { $0.tintColor = Theme.primaryText }

        textField.inputAccessoryView = toolbar
        textField.addTarget(
            self,
            action: #selector(valueChangedAction(_:)),
            for: .editingChanged
        )

        currencyButton.tap = { [weak self] in  self?.currencyAction?() }
    }
}

// MARK: - Constants

private extension CurrencyInputView {

    enum Constant {
        static let defaultHeight: CGFloat = 80
    }
}
