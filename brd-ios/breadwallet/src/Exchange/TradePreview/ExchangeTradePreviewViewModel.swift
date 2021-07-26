// 
//  ExchangeTradePreviewViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangeTradePreviewViewModel {

    let header: ExchangeTradePreviewHeaderViewModel
    let infoItems: [InfoItem]
    let footer: String
    let ctaState: CTAState
    let rewardsDetailAction: (() -> Void)?
    let closeAction: (() -> Void)?
    let ctaAction: (() -> Void)?
}

// MARK: - InfoItem

extension ExchangeTradePreviewViewModel {

    enum InfoItem {

        case simple(key: String, value: String, color: UIColor, bold: Bool)
        case feeTotal(value: String, detail: String?, color: UIColor?)
        case fee(key: String, value: String, detail: String?, detailColor: UIColor?)
        case reward(active: Bool)

        var isFee: Bool {
            if case .fee = self {
                return true
            }
            return false
        }

        var isFeeTotal: Bool {
            if case .feeTotal = self {
                return true
            }
            return false
        }

        var isRewards: Bool {
            if case .reward = self {
                return true
            }
            return false
        }
    }
}

// MARK: - Convenience initializer

extension ExchangeTradePreviewViewModel {

    init(
        model: ExchangeModel,
        assetCollection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?,
        rewardsDetailAction: (() -> Void)?
    ) {
        let fromCode = model.sourceCurrencyCode ?? ""
        let toCode = model.quoteCurrencyCode ?? ""
        let assets = [fromCode, toCode]
            .compactMap { model.currencies[$0] }
            .compactMap { CurrencyId(rawValue: $0.currencyId) }
            .compactMap { assetCollection?.allAssets[$0] }

        var offer = model.selectedOffer as? ExchangeModel.OfferDetailsValidOffer

        if let state = model.state as? ExchangeModel.StateProcessingOrder,
           offer == nil {
            offer = state.offerDetails
        }

        if let state = model.state as? ExchangeModel.StateOrderComplete,
           offer == nil {
            offer = state.offerDetails
        }
        
        var items: [InfoItem] = [
            .simple(
                key: S.Exchange.from,
                value: model.formattedSourceAmount ?? "",
                color: assets.first?.colors.0 ?? Theme.primaryText,
                bold: true
            ),
            .simple(
                key: S.Exchange.to,
                value: model.formattedQuoteAmount ?? "",
                color: assets.last?.colors.0 ?? Theme.primaryText,
                bold: true
            )
        ]

         if let delivery = offer?.offer.deliveryEstimate {
             items.append(
                 .simple(
                     key: "Delivery",
                     value: delivery,
                     color: Theme.primaryText,
                     bold: false
                 )
             )
         }

        let invoice = offer?.offer.invoiceEstimate
        let sourceFees = (try? invoice?.sourceCurrency.fees.double()) ?? 0 != 0

        items.append(contentsOf: [
            InfoItem.simple(
                key: S.Exchange.fulfilledBy,
                value: offer?.offer.provider.name ?? "",
                color: Theme.primaryText,
                bold: false
            ),
            // TODO: - Add discount total
            InfoItem.feeTotal(
                value: sourceFees
                    ? offer?.formattedSourceFees ?? ""
                    : offer?.formattedQuoteFees ?? "",
                detail: nil,
                color: UIColor.brdGreen
            ),
            InfoItem.fee(
                key: S.Exchange.Preview.partnerFee,
                value: offer?.formattedProviderFee ?? "",
                detail: ExchangeTradePreviewViewModel.feeMessage(
                    for: .provider,
                    offer: offer
                ),
                detailColor: UIColor.brdGreen
            ),
            InfoItem.fee(
                key: S.Exchange.Preview.networkFee,
                value: offer?.formattedNetworkFee ?? "",
                detail: ExchangeTradePreviewViewModel.feeMessage(
                    for: .network,
                    offer: offer
                ),
                detailColor: UIColor.brdGreen
            ),
            InfoItem.fee(
                key: S.Exchange.Preview.rate,
                value: offer?.formattedSourceRate ?? "",
                detail: nil,
                detailColor: nil
            )
        ])

        let rewardsActive = offer?.offer.invoiceEstimate?.fees
            .flatMap { $0.discounts }
            .first(where: { $0.type == .platform }) != nil

        items.append(contentsOf: [.reward(active: rewardsActive)])

        self.init(
            header: .init(
                fromColors: [assets.first?.colors.0, assets.first?.colors.1],
                toColors: [assets.last?.colors.0, assets.last?.colors.1],
                fromIcon: assets.first?.imageNoBackground,
                toIcon: assets.last?.imageNoBackground,
                fromSymbol: fromCode.uppercased(),
                toSymbol: toCode.uppercased()
            ),
            infoItems: items,
            footer: S.Exchange.Preview.footer,
            ctaState: CTAState.from(model),
            rewardsDetailAction: rewardsDetailAction,
            closeAction: { consumer?.accept(.OnBackClicked()) },
            ctaAction: { consumer?.accept(.OnContinueClicked()) }
        )
    }

    static  func feeMessage(
            for type: ExchangeInvoiceEstimate.FeeType,
            offer: ExchangeModel.OfferDetailsValidOffer?
    ) -> String? {
        guard let fees = offer?.offer.invoiceEstimate?.fees else {
            return nil
        }
        return fees.first(where: {$0.type == type})?.discounts.first?.message
    }
}

// MARK: - CTAState

extension ExchangeTradePreviewViewModel {

    enum CTAState {
        case next
        case processing

        func text() -> String {
            switch self {
            case .next:
                return S.Exchange.Preview.cta
            case .processing:
                return S.Exchange.CTA.processing
            }
        }

        func isEnabled() -> Bool {
            return self != .processing
        }

        static func from(_ model: ExchangeModel) -> CTAState {
            if model.state as? ExchangeModel.StateProcessingOrder != nil {
                return .processing
            }
            return .next
        }
    }
}

// MARK: - Mock

extension ExchangeTradePreviewViewModel {

    static func mock() -> ExchangeTradePreviewViewModel {
        return .init(
            header: ExchangeTradePreviewHeaderViewModel(
                fromColors: [.clear],
                toColors: [.clear],
                fromIcon: UIImage(named: "laika"),
                toIcon: UIImage(named: "laika"),
                fromSymbol: "ETH",
                toSymbol: "BTC"
            ),
            infoItems: [
                .simple(key: "From", value: "1.05432 ETH", color: .blue, bold: true),
                .simple(key: "To", value: "0.0001345 BTC", color: .yellow, bold: true),
                .simple(key: "Delivery", value: "~ 1 to 3 hours", color: Theme.primaryText, bold: false),
                .simple(key: "Fulfilled by", value: "Changelly", color: Theme.primaryText, bold: false),
                .feeTotal(value: "0.00000034ETH", detail: "25% Off", color: .brdGreen),
                .fee(key: "Exchange Fee", value: "0.000002ETH", detail: "25% OFF!", detailColor: .brdGreen),
                .fee(key: "Network Fee", value: "0.0000075ETH", detail: nil, detailColor: nil),
                .fee(key: "Rate", value: "28.23 ETH per BTC", detail: nil, detailColor: nil),
                .reward(active: false)
            ],
            footer: "This is an estimate. The amount you will receive will depend on the market conditions.",
            ctaState: .next,
            rewardsDetailAction: nil,
            closeAction: nil,
            ctaAction: nil
        )
    }
}
