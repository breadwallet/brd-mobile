// 
//  ExchangeTradeViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangeTradeViewModel {

    typealias Action = () -> Void
    typealias CurrencyAction = (_ base: Bool) -> Void

    // Data
    let quoteViewModel: CurrencyInputViewModel
    let baseViewModel: CurrencyInputViewModel
    let offerViewModel: ExchangeTradeOfferViewModel?
    let fromInfo: String?
    let fromInfoColor: UIColor
    let ctaState: CTAState
    let limit: String?
    let emptyWallets: Bool

    // Actions
    let swapAction: Action?
    let offerAction: Action?
    let closeAction: Action?
    let nextAction: Action?
}

// MARK: - ExchangeModel

extension ExchangeTradeViewModel {

    init(
        model: ExchangeModel,
        assetCollection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?,
        responder: Responder
    ) {
        quoteViewModel = ExchangeTradeViewModel.currencyViewModel(
            isQuote: true,
            model: model,
            assetCollection: assetCollection,
            consumer: consumer
        )

        baseViewModel = ExchangeTradeViewModel.currencyViewModel(
            isQuote: false,
            model: model,
            assetCollection: assetCollection,
            consumer: consumer
        )

        offerViewModel = ExchangeTradeOfferViewModel.from(
            model,
            onTap: { consumer?.accept(.OnSelectOfferClicked(cancel: false)) }
        )

        (fromInfo, fromInfoColor) = ExchangeTradeViewModel.fromInfo(
            for: model,
            responder: responder
        )

        ctaState = CTAState.from(model)
        limit =  nil
        emptyWallets = (model.state as? ExchangeModel.StateEmptyWallets) != nil
        swapAction = { consumer?.accept(.OnSwapCurrenciesClicked()) }
        offerAction = { consumer?.accept(.OnSelectOfferClicked(cancel: false)) }
        closeAction = { consumer?.accept(.OnCloseClicked(confirmed: false))}
        nextAction = {
            let retry = CTAState.from(model) == .retry
            consumer?.accept(
                consumer?.accept(retry ? .OnDialogConfirmClicked() : .OnContinueClicked())
            )
        }
    }

    static func currencyViewModel(
        isQuote: Bool,
        model: ExchangeModel,
        assetCollection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?
    ) -> CurrencyInputViewModel {
        guard let code = isQuote ? model.sourceCurrencyCode : model.quoteCurrencyCode else {
            return .empty({ consumer?.accept(.OnSelectPairClicked(selectSource: isQuote)) })
        }

        guard let currency = model.currencies[code] else {
            return .empty({ consumer?.accept(.OnSelectPairClicked(selectSource: isQuote)) })
        }

        let currencyId = CurrencyId(rawValue: currency.currencyId)
        let allAssets = assetCollection?.allAssets
        let metadata = allAssets?[currencyId]
        var detail: String?

        let dollarRate = model.pairs
            .filter { $0.fromCode == "usdc" && $0.toCode == code }
            .first

        if let fiatValue = model.formattedSourceAmountFiatValue, isQuote {
            detail = "≈ \(fiatValue)"
        } else if let rate = dollarRate?.rate, isQuote {
            let fiatValue = rate * ((try? model.sourceAmountInput.double()) ?? 0.0)
            detail = "≈ \(CommonFormatter.price.string(from: fiatValue) ?? "")"
        }

        func wrap(_ event: ExchangeEvent.OnAmountChange, quote: Bool) -> ExchangeEvent {
            return quote ? event : .OnQuoteAmountChange(amountChange: event)
        }

        let text = isQuote ? model.sourceAmountInput : model.formattedQuoteAmount
        let decSeparator = NumberFormatter().decimalSeparator ?? ""

        return .init(
            text: isQuote ? text : text?.filter("0123456789.".contains),
            detail: detail,
            icon: metadata?.imageNoBackground,
            symbol: code.uppercased(),
            bgColor: (metadata?.colors.0, metadata?.colors.1),
            inputEnabled: isQuote,
            didChangeAction: { (old, new) in
                let new = new.replacingOccurrences(of: decSeparator, with: ".")
                var event: ExchangeEvent.OnAmountChange?
                if new.count > old.count, let digit = new.lastCharacterAsInt() {
                    event = .OnAmountChangeDigit(digit: Int32(digit))
                } else if new.count > old.count, String(new.suffix(1)) == "." {
                    event = .OnAmountChangeDecimal()
                } else if new.count < old.count {
                    event = .OnAmountChangeDelete()
                }
                if let unwrappedEvent = event {
                    consumer?.accept(wrap(unwrappedEvent, quote: isQuote))
                }
            },
            didEndEditingAction: { _ in () },
            clearAction: { _ in
                consumer?.accept(wrap(.OnAmountChangeClear(), quote: isQuote))
            },
            minAction: { consumer?.accept(.OnMinAmountClicked()) },
            maxAction: { consumer?.accept(.OnMaxAmountClicked()) },
            currencyAction: {
                let source = model.selectedPair == nil ? false : isQuote
                consumer?.accept(.OnSelectPairClicked(selectSource: source))
            }
        )
    }
}

// MARK: - CTAState

extension ExchangeTradeViewModel {

    enum CTAState: Equatable {
        case nextDisabled
        case nextEnabled
        case retry
        case nextSetMin(min: String)
        case nextSetMax(max: String)
        case processing

        func text() -> String {
            switch self {
            case .nextDisabled:
                return S.Exchange.CTA.preview
            case .nextEnabled:
                return S.Exchange.CTA.preview
            case .retry:
                return S.Exchange.CTA.retry
            case let .nextSetMin(min):
                return String(format: S.Exchange.CTA.setMin, min)
            case let .nextSetMax(max):
                return String(format: S.Exchange.CTA.setMax, max)
            case .processing:
                return S.Exchange.CTA.loading
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

extension ExchangeTradeViewModel.CTAState {

    static func from(_ model: ExchangeModel) -> ExchangeTradeViewModel.CTAState {
        if model.offerState == .gathering {
            return .processing
        }

        if model.selectedOffer == nil {
            return .nextDisabled
        }

        if model.isInAny(of: processingStates()) {
            return .processing
        }

        let invalid = model.selectedOffer as? ExchangeModel.OfferDetailsInvalidOffer
        let balance = model.cryptoBalances[model.sourceCurrencyCode ?? ""]

        if let minAmount = invalid?.minSourceAmount {
            if let balance = balance, balance.doubleValue < minAmount.doubleValue {
                return .nextDisabled
            }
            if minAmount.doubleValue > model.sourceAmount {
               return .nextSetMin(min: invalid?.formattedMinSourceAmount ?? "")
           }
        }

        if let maxAmount = invalid?.maxSourceAmount,
           maxAmount.doubleValue < model.sourceAmount {
            return .nextSetMax(max: invalid?.formattedMaxSourceAmount ?? "")
        }

        if model.offerState == .noOffers {
            return .retry
        }

        if model.inputError != nil {
            return .nextDisabled
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

// MARK: - FromInfo

extension  ExchangeTradeViewModel {

    enum Responder {
        case base
        case quote
        case none

        var isQuote: Bool {
            self == .quote
        }
    }

    private static func fromInfo(
            for model: ExchangeModel,
            responder: Responder
    ) -> (String?, UIColor) {

        if model.inputError != nil {
            return inputErrorFromInfo(for: model, responder: responder)
        }

        let offer = model.selectedOffer
        let balance = model.cryptoBalances[model.sourceCurrencyCode ?? ""]

        if let invalid = offer as? ExchangeModel.OfferDetailsInvalidOffer {

            let minAmount = invalid.minSourceAmount?.doubleValue ?? 0

            if let balance = balance, balance.doubleValue < minAmount {
                return (
                    S.Exchange.Offer.limitTradeMin + (invalid.formattedMinSourceAmount ?? ""),
                    .failedRed
                )
            }

            if let min = invalid.formattedMinSourceAmount {
                return (S.Exchange.Offer.minAmount + " \(min)", .failedRed)
            }

            if let max = invalid.formattedMaxSourceAmount {
                return (S.Exchange.Offer.minAmount + " \(max)", .failedRed)
            }
        }

        let formattedBalances = model.formattedCryptoBalances
        let formattedBalance = formattedBalances[model.sourceCurrencyCode ?? ""]
        return (
            balance == nil ? "" : String(format: S.Exchange.available, formattedBalance ?? ""),
            Theme.tertiaryText
        )
    }

    private static func inputErrorFromInfo(
            for model: ExchangeModel,
            responder: Responder
    ) -> (String?, UIColor) {

        let baseCode = model.sourceCurrencyCode
        let quoteCode = model.quoteCurrencyCode
        let formattedBalances = model.formattedCryptoBalances

        guard let code = responder.isQuote ? baseCode : quoteCode,
              let balance = model.cryptoBalances[code]?.doubleValue else {
            return ("Input error", .failedRed)
        }

        if model.sourceAmount > balance && responder != .quote ||
           model.quoteAmount?.doubleValue ?? 0 > balance && responder != .base {
            return (
                S.Exchange.Offer.insufficientBalance + (formattedBalances[code] ?? ""),
                .failedRed
            )
        }

        return ("Input error", .failedRed)
    }
}
