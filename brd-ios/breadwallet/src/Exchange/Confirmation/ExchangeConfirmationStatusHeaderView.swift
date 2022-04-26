//
//  ExchangeStatusHeaderView.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeConfirmationStatusHeaderView: UIView {

    var successfulColorOverride: UIColor?
    var confettyStyle: ConfettyView.Style = .default

    private(set) var state: State = .processing

    private lazy var imageView = UIImageView()
    private lazy var label = UILabel()
    private lazy var confettyView = ConfettyView(frame: .zero)
    private lazy var gradientLayer = CAGradientLayer()

    init() {
        super.init(frame: .zero)
        setupUI()
    }

    func setState(_ state: State) {
        guard state != self.state else {
            return
        }
        self.state = state
        self.update(to: state)
    }

    func animateConfetty() {
        confettyView.style = confettyStyle
        confettyView.animateConfetty()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.bounds = bounds
        gradientLayer.position = bounds.center
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - State

extension ExchangeConfirmationStatusHeaderView {

    enum State {
        case creating
        case processing
        case failed
        case sent
        case successful
    }
}

// MARK: - UI setup

private extension ExchangeConfirmationStatusHeaderView {

    func setupUI() {
        let spacers = [UIView(), UIView()]
        let container = VStackView([spacers[0], imageView, label, spacers[1]])
        addSubview(container)
        addSubview(confettyView)
        container.constrain(toSuperviewEdges: nil)
        container.setCustomSpacing(C.padding[3], after: imageView)
        container.alignment = .center
        container.distribution = .fill

        container.constrain([
            spacers[1].heightAnchor.constraint(equalTo: heightAnchor, multiplier: 0.14)
        ])

        imageView.tintColor = Theme.primaryText
        imageView.constrain([
            imageView.constraint(.width, constant: Constant.iconSize),
            imageView.constraint(.height, constant: Constant.iconSize)
        ])
        
        label.textColor = Theme.primaryText
        label.font = Theme.h0Title
        confettyView.constrain(toSuperviewEdges: nil)

        let gradient = gradientLayer
        gradient.colors = [UIColor.fromHex("41BB85"), UIColor.fromHex("26B645")]
                .map { $0.cgColor }
        gradient.startPoint = CGPoint(x: 0, y: 1.25)
        gradient.endPoint = CGPoint(x: 1.2, y: -1)
        gradient.type = .axial
        layer.insertSublayer(gradient, at: 0)
    }

    func update(to state: State) {
        switch state {
        case .creating:
            backgroundColor = .clear
            imageView.removeActivityIndicator()
            imageView.image = nil
            label.text = S.Exchange.Order.Status.creating
        case .processing:
            backgroundColor = .brdYellow
            imageView.addActivityIndicator(.whiteLarge)
            label.text = S.Exchange.Order.Status.processing
        case .failed:
            backgroundColor = .brdRed
            imageView.removeActivityIndicator()
            imageView.image = UIImage(named: "deletecircle")
            label.text = S.Exchange.Order.Status.failed
        case .sent:
            backgroundColor = successfulColorOverride ?? .brdGreen
            imageView.removeActivityIndicator()
            imageView.image = UIImage(named: "CircleCheckSolid")
            label.text = S.Exchange.Order.Status.sent
        case .successful:
            backgroundColor = .clear
            imageView.removeActivityIndicator()
            imageView.image = UIImage(named: "CircleCheckSolidLarge")
            label.text = S.Exchange.Order.Status.successful
        }
    }
}

// MARK: - Constant

private extension ExchangeConfirmationStatusHeaderView {

    enum Constant {
        static let iconSize = CGFloat(75)
    }
}
