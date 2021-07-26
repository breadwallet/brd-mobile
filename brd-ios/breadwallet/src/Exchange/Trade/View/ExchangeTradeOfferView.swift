//
//  OfferView.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeTradeOfferView: UIView {

    private let noOfferView = CellLayoutButton(accessoryStyle: .large)
    private let loadingView = UIView()
    private let offerView = UIView()
    private let container = VStackView()
    private let titleView = CellLayoutView()
    private let rateLabel = UILabel()
    private let feesLabel = UILabel()
    private var tap: (() -> Void)?

    init() {
        super.init(frame: .zero)
        setupUI()
    }

    func update(with viewModel: ExchangeTradeOfferViewModel?) {
        guard let viewModel = viewModel else {
            hideAll(except: nil)
            return
        }
        switch viewModel {
        case let .noOffer(noOffer):
            hideAll(except: noOfferView)
            noOfferView.update(with: noOffer)
        case .loading:
            hideAll(except: loadingView)
        case let .offer(offer):
            hideAll(except: offer != nil ? offerView : nil)
            updateOfferView(offer)
        default:
            hideAll(except: nil)
        }
        invalidateIntrinsicContentSize()
        backgroundColor = !viewModel.isNoOffer()
            ? Theme.quaternaryBackground
            : .clear
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        if !noOfferView.isHidden || !loadingView.isHidden {
            size.height = Constant.nonOfferHeight
        } else {
            size.height = max(Constant.offerHeight, size.height)
        }
        return size
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - UI setup

private extension ExchangeTradeOfferView {

    func setupUI() {
        container.translatesAutoresizingMaskIntoConstraints = false
        addSubview(container)
        container.constrain(toSuperviewEdges: nil)
        container.addArrangedSubviews([offerView, noOfferView, loadingView])
        setupOfferView()
        setupNoOfferView()
        setupLoadingView()
    }

    func setupOfferView() {
        let rateTitleLabel = UILabel(text: S.Exchange.Offer.rate)
        let feesTitleLabel = UILabel(text: S.Exchange.Offer.fees)
        let spacer = UIView()
        let stack = VStackView([
            titleView,
            HStackView([rateTitleLabel, UIView(), rateLabel]),
            spacer,
            HStackView([feesTitleLabel, UIView(), feesLabel])
        ])

        stack.translatesAutoresizingMaskIntoConstraints = false
        offerView.isUserInteractionEnabled = false
        offerView.addSubview(stack)

        let padding = C.padding[1] + Padding.half
        let insets = UIEdgeInsets(forConstrains: padding, vVal: padding)
        stack.constrain(toSuperviewEdges: insets)
        stack.spacing = padding

        addConstraint(spacer.heightAnchor.constraint(equalToConstant: 0.5))
        spacer.backgroundColor = Theme.primaryText.withAlphaComponent(0.2)

        layer.cornerRadius = Padding.half

        titleView.backgroundColor = .clear
        titleView.iconStyle = .square(size: Constant.iconSize)

        [rateTitleLabel, feesTitleLabel].forEach {
            $0.font = Theme.body1
            $0.textColor = Theme.secondaryText
        }

        [rateLabel, feesLabel].forEach {
            $0.font = Theme.body1
            $0.textColor = Theme.primaryText
        }

        addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(tapped(_:)))
        )
    }

    func setupNoOfferView() {
        noOfferView.alpha = 0.75
        noOfferView.titleStyle = .alwaysProminentTitle
    }

    func setupLoadingView() {
        let spinner = UIActivityIndicatorView(style: .white)
        spinner.translatesAutoresizingMaskIntoConstraints = false
        loadingView.addSubview(spinner)
        spinner.constrain([
            spinner.centerXAnchor.constraint(equalTo: loadingView.centerXAnchor),
            spinner.centerYAnchor.constraint(equalTo: loadingView.centerYAnchor)
        ])
        spinner.startAnimating()
    }

    func updateOfferView(_ offer: ExchangeTradeOfferViewModel.Offer?) {
        titleView.update(with: .init(title: offer?.name, iconImage: offer?.icon)) // iconURL: viewModel?.iconURL)
        rateLabel.text = offer?.rate
        feesLabel.text = offer?.fees
        tap = offer?.tap
    }

    func hideAll(except: UIView?) {
        guard let except = except else {
            isHidden = true
            return
        }

        isHidden = false

        [noOfferView, loadingView, offerView].forEach {
            $0.isHidden = except != $0
        }
    }

    @objc func tapped(_ sender: Any?) {
        tap?()
    }
}

// MARK: - Constant

private extension ExchangeTradeOfferView {

    enum Constant {
        static let iconSize: CGFloat = 28
        static let nonOfferHeight: CGFloat = 54
        static let offerHeight: CGFloat = 129
    }
}
