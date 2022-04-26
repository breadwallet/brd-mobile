//
//  HomeScreenCell.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-11-28.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

protocol HighlightableCell {
    func highlight()
    func unhighlight()
}

enum HomeScreenCellIds: String {
    case regularCell        = "CurrencyCell"
    case highlightableCell  = "HighlightableCurrencyCell"
}

class Background: UIView, GradientDrawable {

    var currency: Currency?

    override func layoutSubviews() {
        super.layoutSubviews()
        let maskLayer = CAShapeLayer()
        let corners: UIRectCorner = .allCorners
        maskLayer.path = UIBezierPath(roundedRect: bounds, byRoundingCorners: corners,
                                      cornerRadii: CGSize(width: C.Sizes.homeCellCornerRadius,
                                                          height: C.Sizes.homeCellCornerRadius)).cgPath
        layer.mask = maskLayer
    }

    override func draw(_ rect: CGRect) {
        guard let currency = currency else { return }
        let colors = currency.isSupported ? currency.colors : (UIColor.disabledCellBackground, UIColor.disabledCellBackground)
        drawGradient(start: colors.0, end: colors.1, rect)
    }
}

class HomeScreenCell: UITableViewCell, Subscriber {
    
    private let iconContainer = UIView(color: .transparentIconBackground)
    private let icon = UIImageView()
    private let currencyName = UILabel(font: Theme.body1Accent, color: Theme.primaryText)
    private let price = UILabel(font: Theme.body3, color: Theme.secondaryText)
    private let fiatBalance = UILabel(font: Theme.body1Accent, color: Theme.primaryText)
    private let tokenBalance = UILabel(font: Theme.body3, color: Theme.secondaryText)
    private let syncIndicator = SyncingIndicator(style: .home)
    private let priceChangeView = PriceChangeView(style: .percentOnly)
    private let backgroundOverlayImageView = UIImageView()

    let container = Background()    // not private for inheritance
        
    private var isSyncIndicatorVisible: Bool = false {
        didSet {
            UIView.crossfade(tokenBalance, syncIndicator, toRight: isSyncIndicatorVisible, duration: isSyncIndicatorVisible == oldValue ? 0.0 : 0.3)
            fiatBalance.textColor = (isSyncIndicatorVisible || !(container.currency?.isSupported ?? false)) ? .disabledWhiteText : .white
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupViews()
    }

    static func cellIdentifier() -> String {
        return "CurrencyCell"
    }
    
    func set(viewModel: HomeScreenAssetViewModel) {
        accessibilityIdentifier = viewModel.currency.name
        container.currency = viewModel.currency
        icon.image = viewModel.currency.imageNoBackground
        icon.tintColor = viewModel.currency.isSupported ? .white : .disabledBackground
        iconContainer.layer.cornerRadius = C.Sizes.homeCellCornerRadius
        currencyName.text = viewModel.currency.name
        currencyName.textColor = viewModel.currency.isSupported ? .white : .disabledWhiteText
        price.text = viewModel.exchangeRate
        fiatBalance.text = viewModel.fiatBalance
        fiatBalance.textColor = viewModel.currency.isSupported ? .white : .disabledWhiteText
        tokenBalance.text = viewModel.tokenBalance
        priceChangeView.isHidden = false
        priceChangeView.currency = viewModel.currency
        container.setNeedsDisplay()
        Store.subscribe(self, selector: { $0[viewModel.currency]?.syncState != $1[viewModel.currency]?.syncState },
                        callback: { state in
                            if viewModel.currency.isHBAR && (Store.state.requiresCreation(viewModel.currency)) {
                                self.isSyncIndicatorVisible = false
                                return
                            }
                            guard let syncState = state[viewModel.currency]?.syncState else { return }
                            self.syncIndicator.syncState = syncState
                            switch syncState {
                            case .connecting, .failed, .syncing:
                                self.isSyncIndicatorVisible = true
                            case .success:
                                self.isSyncIndicatorVisible = false
                            }
        })
        
        Store.subscribe(self, selector: { $0[viewModel.currency]?.syncProgress != $1[viewModel.currency]?.syncProgress },
                        callback: { state in
                            if let progress = state[viewModel.currency]?.syncProgress {
                                self.syncIndicator.progress = progress
                            }
        })

        backgroundOverlayImageView.image = viewModel.backgroundOverlayImage
    }
    
    func setupViews() {
        addSubviews()
        addConstraints()
        setupStyle()
    }

    private func addSubviews() {
        contentView.addSubview(container)
        container.addSubview(backgroundOverlayImageView)
        container.addSubview(iconContainer)
        iconContainer.addSubview(icon)
        container.addSubview(currencyName)
        container.addSubview(price)
        container.addSubview(fiatBalance)
        container.addSubview(tokenBalance)
        container.addSubview(syncIndicator)
        container.addSubview(priceChangeView)
        syncIndicator.isHidden = true
    }

    private func addConstraints() {
        let containerPadding = C.padding[1]
        container.constrain(toSuperviewEdges: UIEdgeInsets(top: 0,
                                                           left: containerPadding,
                                                           bottom: -C.padding[1],
                                                           right: -containerPadding))
        iconContainer.constrain([
            iconContainer.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: containerPadding),
            iconContainer.centerYAnchor.constraint(equalTo: container.centerYAnchor),
            iconContainer.heightAnchor.constraint(equalToConstant: 40),
            iconContainer.widthAnchor.constraint(equalTo: iconContainer.heightAnchor)])
        icon.constrain(toSuperviewEdges: .zero)
        currencyName.constrain([
            currencyName.leadingAnchor.constraint(equalTo: iconContainer.trailingAnchor, constant: containerPadding),
            currencyName.bottomAnchor.constraint(equalTo: icon.centerYAnchor, constant: 0.0)])
        price.constrain([
            price.leadingAnchor.constraint(equalTo: currencyName.leadingAnchor),
            price.topAnchor.constraint(equalTo: currencyName.bottomAnchor)])
        priceChangeView.constrain([
            priceChangeView.leadingAnchor.constraint(equalTo: price.trailingAnchor),
            priceChangeView.centerYAnchor.constraint(equalTo: price.centerYAnchor),
            priceChangeView.heightAnchor.constraint(equalToConstant: 24)])
        fiatBalance.constrain([
            fiatBalance.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -containerPadding),
            fiatBalance.leadingAnchor.constraint(greaterThanOrEqualTo: currencyName.trailingAnchor, constant: C.padding[1]),
            fiatBalance.topAnchor.constraint(equalTo: currencyName.topAnchor)])
        tokenBalance.constrain([
            tokenBalance.trailingAnchor.constraint(equalTo: fiatBalance.trailingAnchor),
            tokenBalance.leadingAnchor.constraint(greaterThanOrEqualTo: priceChangeView.trailingAnchor, constant: C.padding[1]),
            tokenBalance.bottomAnchor.constraint(equalTo: price.bottomAnchor)])
        tokenBalance.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        fiatBalance.setContentCompressionResistancePriority(.required, for: .vertical)
        fiatBalance.setContentCompressionResistancePriority(.required, for: .horizontal)
        
        syncIndicator.constrain([
            syncIndicator.trailingAnchor.constraint(equalTo: fiatBalance.trailingAnchor),
            syncIndicator.leadingAnchor.constraint(greaterThanOrEqualTo: priceChangeView.trailingAnchor, constant: C.padding[1]),
            syncIndicator.bottomAnchor.constraint(equalTo: tokenBalance.bottomAnchor, constant: 0.0)])
        syncIndicator.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        layoutIfNeeded()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let imageSize = backgroundOverlayImageView.image?.size ?? CGSize(width: 1, height: 1)
        let size = CGSize(
            width: bounds.height * imageSize.width / imageSize.height,
            height: bounds.height
        )
        let origin = CGPoint(x: bounds.width - size.width, y: 0)
        backgroundOverlayImageView.frame = CGRect(origin: origin, size: size)
    }

    private func setupStyle() {
        selectionStyle = .none
        backgroundColor = .clear
        iconContainer.layer.cornerRadius = 6.0
        iconContainer.clipsToBounds = true
        icon.tintColor = .white
        backgroundOverlayImageView.contentMode = .scaleAspectFit
    }
    
    override func prepareForReuse() {
        Store.unsubscribe(self)
    }
    
    deinit {
        Store.unsubscribe(self)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
