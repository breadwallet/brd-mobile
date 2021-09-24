//
//  NumberKeyboardView.swift
//  breadwallet
// 
//  Created by blockexplorer on 13/04/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class NumberKeyboardView: UIView {

    var textColor: UIColor = Theme.primaryText.withAlphaComponent(0.7)
    var textColorHighlighted: UIColor = Theme.primaryText

    var keyTapAction: ((_ key: Key) -> Void)?

    private var containerStack: UIStackView?
    private let haptics = UIImpactFeedbackGenerator(style: .medium)

    init() {
        super.init(frame: .zero)
        setupUI()
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - Key

extension NumberKeyboardView {

    enum Key: Equatable {
        case digit(digit: Int)
        case decimalSeparator
        case backSpace

        func string() -> String {
            switch self {
            case let .digit(digit):
                return "\(digit)"
            case .decimalSeparator:
                return NumberFormatter().currencyDecimalSeparator ?? "."
            case .backSpace:
                return "⌫"
            }
        }

        static func defaultKeys() -> [Key] {
            let digits = [1, 2, 3, 4, 5, 6, 7, 8, 9].map { Key.digit(digit: $0) }
            return digits + [.decimalSeparator, .digit(digit: 0), .backSpace]
        }
    }
}

// MARK: - UI setup

private extension NumberKeyboardView {

    func setupUI() {
        let buttons: [UIButton] = Key.defaultKeys().enumerated().map {
            let key = $0.1
            let button = KeyButton(type: .custom)
            if key == .backSpace {
                button.setImage(UIImage(named: "Delete"), for: .normal)
            } else {
                button.setTitle($0.1.string(), for: .normal)
            }
            button.titleLabel?.font = .customBody(size: 28.0)
            button.setTitleColor(textColor, for: .normal)
            button.setTitleColor(textColorHighlighted, for: .highlighted)
            button.clipsToBounds = true
            button.tap = { [weak self] in
                self?.didTap(key)
            }
            button.translatesAutoresizingMaskIntoConstraints = false
            return button
        }

        let containerStack = VStackView([
            HStackView(Array(buttons[0..<3]), distribution: .fillEqually),
            HStackView(Array(buttons[3..<6]), distribution: .fillEqually),
            HStackView(Array(buttons[6..<9]), distribution: .fillEqually),
            HStackView(Array(buttons[9..<12]), distribution: .fillEqually)
        ])

        containerStack.distribution = .fillEqually
        containerStack.spacing = C.padding[1]
        containerStack.arrangedSubviews
            .forEach { ($0 as? UIStackView)?.spacing = C.padding[1] }

        addSubview(containerStack)
        containerStack.constrain(toSuperviewEdges: nil)
        self.containerStack = containerStack
    }

    func didTap(_ key: Key) {
        haptics.impactOccurred()
        keyTapAction?(key)
    }
}

// MARK: - KeyButton

private extension NumberKeyboardView {

    class KeyButton: UIButton {

        override var isHighlighted: Bool {
            get { super.isHighlighted }
            set {
                if isHighlighted != newValue {
                    animateToHighlighted(newValue)
                }
                super.isHighlighted = newValue
            }
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            layer.cornerRadius = bounds.height / 2
        }

        func animateToHighlighted(_ highlighted: Bool) {
            UIView.spring(
                C.animationDuration,
                animations: {
                    self.transform = highlighted ? .init(scaleX: 1.1, y: 1.1) : .identity
                    self.backgroundColor = highlighted ? .blue : .clear
                },
                completion: { _ in () }
            )
        }
    }
}
