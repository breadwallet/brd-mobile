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
    let inputPresets: [String]
    let selectedInputPreset: Int?
    let baseText: String?
    let baseColor: UIColor?
    let baseView: CellLayoutView.ViewModel
    let offer: CellLayoutView.ViewModel
    let offerEnabled: Bool
    let offerIsLoading: Bool
    let ctaState: CTAState
    let limit: String?
    let isLoading: Bool
    let fullScreenErrorStyle: ExchangeFullScreenErrorView.Style?

    // Actions
    let settingsAction: Action?
    let inputPresetAction: (Int) -> Void
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
        let isSell = model.mode == .sell
        let quoteCode = model.quoteCurrencyCode
        let baseCode = model.sourceCurrencyCode ?? Store.state.defaultCurrencyCode
        let baseSymbol = Locale.currencySymbolByCode(baseCode) ?? baseCode

        if isSell {
            quoteText = model.sourceAmountInput
        } else {
            let sourceAmount = model.formattedSourceAmount
            quoteText = model.sourceAmountInput == "" ? nil : sourceAmount
        }

        title = ExchangeBuySellViewModel.title(model)
        quotePlaceholder = ExchangeBuySellViewModel.placeholder(
            isSell ? "0" : baseSymbol + "0"
        )
        (baseText, baseColor) = ExchangeBuySellViewModel.baseTextAndColor(model)
        inputPresets = model.inputPresets.map { $0.formattedAmount }
        selectedInputPreset = model.selectedInputPreset?.intValue

        let currency = model.currencies[ isSell ? baseCode : quoteCode ?? ""]
        let currencyId = CurrencyId(rawValue: currency?.currencyId ?? "")
        let loading = (model.state as? ExchangeModel.StateInitializing) != nil
        let sourceBalance = model.formattedCryptoBalances[baseCode] ?? ""
        let formattedSourceBalance = String(format: S.Exchange.available, sourceBalance)
        let lowBalance = model.inputError as? ExchangeModel.InputErrorBalanceLow != nil

        if loading {
            baseView = .init(
                title: S.Exchange.loadingAssets,
                iconImage: UIImage(named: "TransparentPixel")
            )
        } else {
            baseView = .init(
                title: currency?.name ?? "",
                subtitle: isSell ? formattedSourceBalance : nil,
                subtitleColor: isSell && lowBalance ? Theme.error : nil,
                iconImage: assetCollection?.allAssets[currencyId]?
                    .imageSquareBackground,
                rightIconImage: UIImage(named: "RightArrow")
            )
        }

        let fullScreenError: ExchangeFullScreenErrorView.Style?
        if let emptyWalletsState = model.state as? ExchangeModel.StateEmptyWallets {
            if emptyWalletsState.sellingUnavailable {
                fullScreenError = .sellUnsupportedRegion
            } else if emptyWalletsState.invalidSellPairs {
                fullScreenError = .emptyWallets
            } else {
                fullScreenError = .emptyWallets
            }
        } else {
            fullScreenError = nil
        }

        let ctaState = CTAState.from(model)
        offer = ExchangeBuySellViewModel.offer(model)
        offerEnabled = model.offerState != .idle && model.offerState != .noOffers
        offerIsLoading = model.offerState == .gathering
        self.ctaState = ctaState
        limit = nil
        isLoading = loading
        fullScreenErrorStyle = fullScreenError
        settingsAction = { consumer?.accept(.OnConfigureSettingsClicked()) }
        inputPresetAction = { consumer?.accept(.OnSelectInputPresets(index: Int32($0))) }
        closeAction = { consumer?.accept(.OnCloseClicked(confirmed: true)) }
        baseAction = { consumer?.accept(.OnSelectPairClicked(selectSource: model.mode == .sell)) }
        offerAction = { consumer?.accept(.OnSelectOfferClicked(cancel: false)) }
        keyAction = { consumer?.accept($0.exchangeEvent()) }
        ctaAction = {
            switch ctaState {
            case .nextSetMax:
                consumer?.accept(.OnMaxAmountClicked())
            default:
                consumer?.accept(.OnContinueClicked())
            }
        }
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
                    .font: UIFont.customBold(size: 60),
                    .foregroundColor: Theme.primaryText.withAlphaComponent(0.3)
                ]
        )
    }

    static func offer(_ model: ExchangeModel) -> CellLayoutView.ViewModel {
        let method = model.selectedOffer?.formattedViaMethod() ?? ""
        let name = String(format: method, model.selectedOffer?.offer.provider.name ?? "Unknown")

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
            if let error = model.inputError as? ExchangeModel.InputErrorInsufficientNativeCurrencyBalance {
                // TODO: Localise
                return .init(
                    title: String(format: "%@ balance low", error.currencyCode),
                    subtitle: String(format: "Network fee of %@ %@ required ", error.currencyCode),
                    iconImage: UIImage(named: "alertErrorLarge")
                )
            }
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

        if let inputError = model.inputError as? ExchangeModel.InputErrorBalanceLow,
            model.mode == .sell {
            return inputErrorFromInfo(for: model)
        }

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

    static func inputErrorFromInfo(for model: ExchangeModel) -> (String, UIColor) {
        let formattedBalances = model.formattedCryptoBalances
        let code = model.sourceCurrencyCode ?? ""

        guard let balance =  model.cryptoBalances[code]?.doubleValue else {
            return ("Input error", .failedRed)
        }

        let suffix = model.mode != .sell ? (formattedBalances[code] ?? "") : ""
        if model.sourceAmount > balance {
            return (
                S.Exchange.Offer.insufficientBalance + suffix,
                .failedRed
            )
        }

        return ("Input error", .failedRed)
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
        if let inputError = model.inputError as? ExchangeModel.InputErrorBalanceLow,
           model.mode == .sell {
            let balance = model.formattedCryptoBalances[model.sourceCurrencyCode ?? ""]
            return .nextSetMax(max: balance ?? "")
        }

        if model.selectedOffer == nil {
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
