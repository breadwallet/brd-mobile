//
//  ExchangeBuySellViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangeBuySellViewModel {

    typealias Action = () -> Void

    // Data
    let title: String
    let quoteText: String?
    let quotePlaceholder: NSAttributedString?
    let baseText: String?
    let baseColor: UIColor?
    let baseView: CellLayoutView.ViewModel
    let offer: CellLayoutView.ViewModel
    let offerEnabled: Bool
    let offerIsLoading: Bool
    let ctaState: CTAState
    let limit: String?
    let isLoading: Bool

    // Actions
    let settingsAction: Action?
    let closeAction: Action?
    let baseAction: Action?
    let offerAction: Action?
    let keyAction: (NumberKeyboardView.Key) -> Void
    let ctaAction: Action?
}

// MARK: - Convenience initializer

extension ExchangeBuySellViewModel {

    init(
        model: ExchangeModel,
        assetCollection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?
    ) {
        let quoteCode = model.quoteCurrencyCode
        let baseCode = model.sourceCurrencyCode ?? Store.state.defaultCurrencyCode
        let baseSymbol = Locale.currencySymbolByCode(baseCode) ?? baseCode ?? ""
        let baseString = model.formattedSourceAmount

        title = ExchangeBuySellViewModel.title(model)
        quoteText = model.sourceAmount == 0 ? nil : baseString
        quotePlaceholder = ExchangeBuySellViewModel.placeholder(baseSymbol + "0")
        (baseText, baseColor) = ExchangeBuySellViewModel.baseTextAndColor(model)

        let currency = model.currencies[quoteCode ?? ""]
        let currencyId = CurrencyId(rawValue: currency?.currencyId ?? "")
        let isLoading = (model.state as? ExchangeModel.StateInitializing) != nil

        if isLoading {
            baseView = .init(
                title: S.Exchange.loadingAssets,
                iconImage: UIImage(named: "TransparentPixel")
            )
        } else {
            baseView = .init(
                title: currency?.name ?? "",
                iconImage: assetCollection?.allAssets[currencyId]?
                    .imageSquareBackground,
                rightIconImage: UIImage(named: "RightArrow")
            )
        }

        offer = ExchangeBuySellViewModel.offer(model)
        offerEnabled = model.offerState != .idle && model.offerState != .noOffers
        offerIsLoading = model.offerState == .gathering
        ctaState = CTAState.from(model)
        limit = nil
        settingsAction = { consumer?.accept(.OnConfigureSettingsClicked()) }
        closeAction = { consumer?.accept(.OnCloseClicked(confirmed: true)) }
        baseAction = { consumer?.accept(.OnSelectPairClicked(selectSource: false)) }
        offerAction = { consumer?.accept(.OnSelectOfferClicked(cancel: false)) }
        keyAction = { consumer?.accept($0.exchangeEvent()) }
        ctaAction = { consumer?.accept(.OnContinueClicked()) }
        self.isLoading = isLoading
    }
}

// MARK: - Utilities

private extension ExchangeBuySellViewModel {

    static func title(_ model: ExchangeModel) -> String {
        switch model.mode {
        case .buy:
            return S.Exchange.buy
        case .sell:
            return S.Exchange.sell
        default:
            return ""
        }
    }

    static func placeholder(_ string: String) -> NSAttributedString {
        NSAttributedString(
            string: string,
            attributes: [
                .font: UIFont.customBold(size: 60) ?? Theme.body1,
                .foregroundColor: Theme.primaryText.withAlphaComponent(0.3)
            ]
        )
    }

    static func offer(_ model: ExchangeModel) -> CellLayoutView.ViewModel {
        let method = model.selectedOffer?.formattedViaMethod() ?? ""
        let name = String(format: method, model.selectedOffer?.offer.provider.name ?? "Unknown")
        let valid = model.selectedOffer as? ExchangeModel.OfferDetailsValidOffer

        switch model.offerState {
        case .noOffers:
            return .init(
                title: S.Exchange.Offer.noneTitle,
                subtitle: S.Exchange.Offer.noneSubtitle,
                iconImage: UIImage(named: "OfferNone")
            )
        case .gathering:
            return .init(
                title: S.Exchange.Offer.gatheringTitle,
                subtitle: S.Exchange.Offer.gatheringSubtitle,
                iconImage: UIImage(named: "TransparentPixel"),
                rightIconImage: nil
            )
        case .completed:
            return .init(
                title: name,
                subtitle: subtitle(offer: model.selectedOffer),
                iconImage: UIImage(
                    named: model.selectedOffer?.offer.provider.imageSlug
                        ?? "unknownProvider"
                ),
                // iconURL: logoUrl
                rightIconImage: model.offerDetails.count > 1
                        ? UIImage(named: "RightArrow")
                        : nil
          )
        default:
            return .init(
                title: S.Exchange.Offer.initTitle,
                iconImage: UIImage(named: "OfferCart")
            )
        }
    }

    static func subtitle(offer: ExchangeModel.OfferDetails?) -> String? {
        guard let valid = offer as? ExchangeModel.OfferDetailsValidOffer else {
            let invalid = offer as? ExchangeModel.OfferDetailsInvalidOffer

            if let min = invalid?.formattedMinSourceAmount {
                return S.Exchange.Offer.minAmount + " \(min)"
            }

            if let max = invalid?.formattedMaxSourceAmount {
                return S.Exchange.Offer.maxAmount + " \(max)"
            }
            return nil
        }

        let rate = valid.formattedSourceRate ?? "unknown"
        let total = valid.formattedSourceTotal 
        return S.Exchange.Offer.rate + " \(rate) - \(S.Exchange.Offer.total) \(total)"
    }

    static func baseTextAndColor(_ model: ExchangeModel) -> (String, UIColor) {
        let invalid = model.selectedOffer as? ExchangeModel.OfferDetailsInvalidOffer

        if let minAmount = invalid?.minSourceAmount,
           minAmount.doubleValue > model.sourceAmount {
            let amount = invalid?.formattedMinSourceAmount ?? ""
            return (
                S.Exchange.under + "  \(amount) " + S.Exchange.limit,
                UIColor.brdRed
            )
        }

        if let maxAmount = invalid?.maxSourceAmount,
           maxAmount.doubleValue < model.sourceAmount {
            let amount = invalid?.formattedMaxSourceAmount ?? ""
            return (
                S.Exchange.over + " \(amount) " + S.Exchange.limit,
                UIColor.brdRed
            )
        }

        return (model.formattedQuoteAmount ?? " ", Theme.secondaryText)
    }
}

// MARK: - CTAState

extension ExchangeBuySellViewModel {

    enum CTAState: Equatable {
        case nextDisabled
        case nextEnabled
        case nextSetMin(min: String)
        case nextSetMax(max: String)
        case processing

        func text() -> String {
            switch self {
            case .nextDisabled:
                return S.Exchange.CTA.next
            case .nextEnabled:
                return S.Exchange.CTA.next
            case let .nextSetMin(min):
                return String(format: S.Exchange.CTA.setMin, min)
            case let .nextSetMax(max):
                return String(format: S.Exchange.CTA.setMax, max)
            case .processing:
                return S.Exchange.CTA.processing
            }
        }

        func isEnabled() -> Bool {
            switch self {
            case .nextDisabled, .processing:
                return false
            default:
                return true
            }
        }
    }
}

extension ExchangeBuySellViewModel.CTAState {

    static func from(_ model: ExchangeModel) -> ExchangeBuySellViewModel.CTAState {
        guard let offer = model.selectedOffer else {
            return .nextDisabled
        }

        if model.isInAny(of: processingStates()) {
             return .processing
        }

        let invalid = model.selectedOffer as? ExchangeModel.OfferDetailsInvalidOffer

        if let minAmount = invalid?.minSourceAmount,
            minAmount.doubleValue > model.sourceAmount {
            return .nextSetMin(min: invalid?.formattedMinSourceAmount ?? "")
        }

        if let maxAmount = invalid?.maxSourceAmount,
           maxAmount.doubleValue < model.sourceAmount {
            return .nextSetMax(max: invalid?.formattedMaxSourceAmount ?? "")
        }

        return .nextEnabled
    }

    static func processingStates() -> [ExchangeModel.State.Type] {
        return [
            ExchangeModel.StateCreatingOrder.self,
            ExchangeModel.StateProcessingOrder.self
        ]
    }
}

// MARK: - NumberKeyboardView.Key

private extension NumberKeyboardView.Key {

    func exchangeEvent() -> ExchangeEvent {
        switch self {
        case let .digit(digit):
            return .OnAmountChangeDigit(digit: Int32(digit))
        case .decimalSeparator:
            return .OnAmountChangeDecimal()
        case .backSpace:
            return .OnAmountChangeDelete()
        }
    }
}
