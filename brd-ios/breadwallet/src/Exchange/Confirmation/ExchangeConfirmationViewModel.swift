//
//  ExchangeOrderViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangeConfirmationViewModel {

    typealias Action = () -> Void

    var state: State
    var footerInfo: String?
    var cancelAction: Action?
    var confirmAction: ((_ canceled: Bool) -> Void)?
    var viewReceiptAction: Action?
    var doneAction: Action?

    init(
        model: ExchangeModel,
        assetCollection: AssetCollection?,
        viewReceiptAction: Action? = nil,
        doneAction: Action? = nil
    ) {
        self.state = State.from(model, assetCollection: assetCollection) ?? .creating
        self.footerInfo = ExchangeConfirmationViewModel.defaultFooterInto()
        self.viewReceiptAction = viewReceiptAction
        self.doneAction = doneAction
    }

    func order() -> Order? {
        switch state {
        case let .processing(order):
            return order
        case let .complete(order):
            return order
        default:
            return nil
        }
    }

    static func defaultFooterInto() -> String {
        return S.Exchange.Order.footer
    }
}

extension ExchangeConfirmationViewModel {

    enum State {
        case creating
        case processing(order: Order)
        case complete(order: Order)

        static func from(_ model: ExchangeModel, assetCollection: AssetCollection?) -> State? {
            if let state = model.state as? ExchangeModel.StateCreatingOrder {
                return .creating
            }
            if let state = model.state as? ExchangeModel.StateProcessingOrder {
                return .processing(
                    order: Order(
                        state.order,
                        offer: state.offerDetails,
                        assetCollection: assetCollection
                    )
                )
            }
            if let state = model.state as? ExchangeModel.StateOrderComplete {
                return .complete(
                    order: Order(
                        state.order,
                        offer: state.offerDetails,
                        assetCollection: assetCollection
                    )
                )
            }
            return nil
        }
    }
}

// MARK: - OrderViewModel

extension ExchangeConfirmationViewModel {

    struct Order {

        let baseAmount: String
        let baseColor: UIColor
        let quoteAmount: String
        let quoteColor: UIColor
        let method: String
        let delivery: String
        let fees: [Fee]
        let feeTotal: String

        init(
            _ order: ExchangeOrder,
            offer: ExchangeModel.OfferDetailsValidOffer,
            assetCollection: AssetCollection?
        ) {
            let assets = [order.inputs.first?.currency, order.outputs.first?.currency]
                .compactMap { $0 }
                .compactMap { CurrencyId(rawValue: $0.currencyId) }
                .compactMap { assetCollection?.allAssets[$0] }
            baseAmount = offer.formattedQuoteTotal
            baseColor = assets.last?.colors.0 ?? Theme.primaryText
            quoteAmount = offer.formattedSourceTotal
            quoteColor = assets.first?.colors.0 ?? Theme.primaryText
            method = String(format: offer.formattedViaMethod(), offer.offer.provider.name ?? "")
            delivery = offer.offer.deliveryEstimate ?? "N/A"
            fees = [
                .init(
                    title: S.Exchange.Offer.processingFee,
                    amount: offer.formattedProviderFee ?? ""
                ),
                .init(
                    title: S.Exchange.Offer.networkFee,
                    amount: offer.formattedNetworkFee ?? ""
                ),
                .init(
                    title: S.Exchange.Offer.providerFee,
                    amount: offer.formattedProviderFee ?? ""
                )
            ]

            let invoice = offer.offer.invoiceEstimate
            let sourceFees = (try? invoice?.sourceCurrency.fees.double()) ?? 0 != 0

            feeTotal = sourceFees
                ? offer.formattedSourceFees ?? ""
                : offer.formattedQuoteFees ?? ""
        }

        init(
            baseAmount: String,
            quoteAmount: String,
            method: String,
            delivery: String,
            fees: [Fee],
            feeTotal: String
        ) {
            self.baseAmount = baseAmount
            self.baseColor = Theme.primaryText
            self.quoteAmount = quoteAmount
            self.quoteColor = Theme.primaryText
            self.method = method
            self.delivery = delivery
            self.fees = fees
            self.feeTotal = feeTotal
        }
    }
}

// MARK: - Fee

extension ExchangeConfirmationViewModel.Order {

    struct Fee {
        let title: String
        let amount: String

        static func mock() -> [Fee] {
            return [
                .init(title: "Mock", amount: "N/A"),
                .init(title: "Mock", amount: "N/A"),
                .init(title: "Mock", amount: "N/A")
            ]
        }
    }
}

// MARK: - Mock

extension  ExchangeConfirmationViewModel {

    init(
        state: ExchangeConfirmationViewModel.State,
        cancelAction: Action? = nil,
        confirmAction: ((_ canceled: Bool) -> Void)? = nil,
        viewReceiptAction: Action? = nil,
        doneAction: Action? = nil
    ) {
        self.state = state
        self.footerInfo = ExchangeConfirmationViewModel.defaultFooterInto()
        self.cancelAction = cancelAction
        self.confirmAction = confirmAction
        self.viewReceiptAction = viewReceiptAction
        self.doneAction = doneAction
    }
}

// MARK: - Mock

extension ExchangeConfirmationViewModel.Order {

    static func mock() -> ExchangeConfirmationViewModel.Order {
        return .init(
            baseAmount: "0.00048241 BTC",
            quoteAmount: "$250 USD",
            method: "Wyre with ApplePay",
            delivery: "Instant",
            fees: Fee.mock(),
            feeTotal: "$15.53 USD"
        )
    }
}
