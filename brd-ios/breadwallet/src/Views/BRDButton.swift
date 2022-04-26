//
//  BRDButton.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-11-15.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

enum ButtonType {
    case primary
    case secondary
    case tertiary
    case blackTransparent
    case darkOpaque
    case secondaryTransparent
    case search
}

private let minTargetSize: CGFloat = 48.0

class BRDButton: UIControl {
    
    init(
        title: String,
        type: ButtonType = .primary,
        image: UIImage? = nil,
        target: Any? = nil,
        action: Foundation.Selector? = nil,
        event: UIControl.Event = .touchUpInside
    ) {
        self.title = title
        self.type = type
        self.image = image
        super.init(frame: .zero)
        accessibilityLabel = title
        setupViews()
        if let action = action {
            addTarget(target, action: action, for: event)
        }
    }

    var isToggleable = false

    var title: String {
        didSet {
            label.text = title
        }
    }
    var image: UIImage? {
        didSet {
            imageView?.image = image
        }
    }

    var cornerRadius: CGFloat {
        get { container.layer.cornerRadius }
        set { container.layer.cornerRadius = newValue }
    }

    private(set) var label = UILabel()

    private let type: ButtonType
    private let container = UIView()
    private var imageView: UIImageView?
    private var activityView = UIActivityIndicatorView(style: .white)

    override var isHighlighted: Bool {
        didSet {
            // Shrinks the button to 97% and drops it down 4 points to give a 3D press-down effect.
            let duration = 0.21
            let scale: CGFloat = 0.97
            let drop: CGFloat = 4.0
            
            if isHighlighted {
                let shrink = CATransform3DMakeScale(scale, scale, 1.0)
                let translate = CATransform3DTranslate(shrink, 0, drop, 0)

                UIView.animate(withDuration: duration, delay: 0, options: .curveEaseInOut, animations: { 
                    self.container.layer.transform = translate
                }, completion: nil)
                
            } else {
                UIView.animate(withDuration: duration, delay: 0, options: .curveEaseInOut, animations: {
                    self.container.transform = CGAffineTransform.identity
                }, completion: nil)
            }
        }
    }

    override var isSelected: Bool {
        didSet {
            guard isToggleable else { return }
            if type == .tertiary || type == .search {
                if isSelected {
                    container.layer.borderColor = UIColor.primaryButton.cgColor
                    imageView?.tintColor = .primaryButton
                    label.textColor = .primaryButton
                } else {
                    setColors()
                }
            }
        }
    }
    
    override var isEnabled: Bool {
        didSet {
            setColors()
        }
    }

    func setActivityViewVisible(_ visible: Bool) {
        visible ? activityView.startAnimating() : activityView.stopAnimating()
    }

    private func setupViews() {
        addContent()
        setColors()
        addTarget(self, action: #selector(BRDButton.touchUpInside), for: .touchUpInside)
        setContentCompressionResistancePriority(UILayoutPriority.required, for: .horizontal)
        label.setContentCompressionResistancePriority(UILayoutPriority.required, for: .horizontal)
        setContentHuggingPriority(UILayoutPriority.required, for: .horizontal)
        label.setContentHuggingPriority(UILayoutPriority.required, for: .horizontal)
        cornerRadius = Constant.defaultCornerRadius
    }

    private func addContent() {
        addSubview(container)
        container.backgroundColor = .primaryButton
        container.isUserInteractionEnabled = false
        container.constrain(toSuperviewEdges: nil)
        label.text = title
        label.textColor = .white
        label.textAlignment = .center
        label.isUserInteractionEnabled = false
        label.font = UIFont.customBody(size: 16.0)
        configureContentType()
        container.addSubview(activityView)
    }

    private func configureContentType() {
        if let icon = image {
            setupImageOption(icon: icon)
        } else {
            setupLabelOnly()
        }
    }

    private func setupImageOption(icon: UIImage) {
        let content = UIView()
        let iconImageView = UIImageView(image: icon.withRenderingMode(.alwaysTemplate))
        iconImageView.contentMode = .scaleAspectFit
        container.addSubview(content)
        content.addSubview(label)
        content.addSubview(iconImageView)
        content.constrainToCenter()
        iconImageView.constrainLeadingCorners()
        label.constrainTrailingCorners()
        iconImageView.constrain([
            iconImageView.constraint(toLeading: label, constant: -C.padding[1]) ])
        imageView = iconImageView
    }

    private func setupLabelOnly() {
        container.addSubview(label)
        label.constrain(toSuperviewEdges: UIEdgeInsets(top: C.padding[1], left: C.padding[1], bottom: -C.padding[1], right: -C.padding[1]))
    }

    private func setColors() {
        switch type {
        case .primary:
            let bgColor = isEnabled ? UIColor.primaryButton : UIColor.primaryButton.withAlphaComponent(0.5)
            container.backgroundColor = bgColor
            label.textColor = isEnabled ? Theme.primaryText : Theme.secondaryText
            container.layer.borderColor = nil
            container.layer.borderWidth = 0.0
            imageView?.tintColor = .white
        case .secondary:
            container.backgroundColor = .secondaryButton
            label.textColor = .darkText
            container.layer.borderColor = UIColor.secondaryBorder.cgColor
            container.layer.borderWidth = 1.0
            imageView?.tintColor = .darkText
        case .tertiary:
            container.backgroundColor = .secondaryButton
            label.textColor = .grayTextTint
            container.layer.borderColor = UIColor.secondaryBorder.cgColor
            container.layer.borderWidth = 1.0
            imageView?.tintColor = .grayTextTint
        case .blackTransparent:
            container.backgroundColor = .clear
            label.textColor = .darkText
            container.layer.borderColor = UIColor.darkText.cgColor
            container.layer.borderWidth = 1.0
            imageView?.tintColor = .grayTextTint
        case .darkOpaque:
            container.backgroundColor = .darkOpaqueButton
            label.textColor = .white
        case .secondaryTransparent:
            container.backgroundColor = .transparentButton
            label.textColor = .white
            container.layer.borderColor = nil
            container.layer.borderWidth = 0.0
            imageView?.tintColor = .white
        case .search:
            label.font = UIFont.customBody(size: 13.0)
            container.backgroundColor = .secondaryButton
            label.textColor = .grayTextTint
            container.layer.borderColor = UIColor.secondaryBorder.cgColor
            container.layer.borderWidth = 1.0
            imageView?.tintColor = .grayTextTint
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        activityView.frame.origin.x = container.bounds.minX + C.padding[1]
        activityView.center.y = container.bounds.midY
    }

    open override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard !isHidden || isUserInteractionEnabled else { return nil }
        let deltaX = max(minTargetSize - bounds.width, 0)
        let deltaY = max(minTargetSize - bounds.height, 0)
        let hitFrame = bounds.insetBy(dx: -deltaX/2.0, dy: -deltaY/2.0)
        return hitFrame.contains(point) ? self : nil
    }

    @objc private func touchUpInside() {
        isSelected = !isSelected
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        size.height = C.Sizes.buttonHeight
        return size
    }
}

extension BRDButton {
    
    enum Constant {
        static let defaultCornerRadius: CGFloat = 2
    }
}
