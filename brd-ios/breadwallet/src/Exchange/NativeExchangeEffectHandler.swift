// 
//  NativeExchangeEffectHandler.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos
import WalletKit

// MARK: - NativeExchangeConnectable

class NativeExchangeConnectable: NSObject, Connectable {

    private let system: CoreSystem
    private let keyStore: KeyStore
    private let isTrade: Bool
    private weak var view: ExchangeView?

    init(
        system: CoreSystem,
        keyStore: KeyStore,
        view: ExchangeView?,
        isTrade: Bool = false
    ) {
        self.system = system
        self.keyStore = keyStore
        self.isTrade = isTrade
        self.view = view
    }

    func connect(output: Consumer) -> Connection {
        NativeExchangeEffectHandler(
            output: output,
            system: system,
            keyStore: keyStore,
            view: view,
            isTrade: isTrade
        )
    }
}

// MARK: - NativeExchangeEffectHandler

class NativeExchangeEffectHandler: NSObject, Connection, Trackable {

    private let output: TypedConsumer<ExchangeEvent>
    private let system: CoreSystem
    private let keyStore: KeyStore
    private let isTrade: Bool
    private weak var view: ExchangeView?

    init(
        output: Consumer,
        system: CoreSystem,
        keyStore: KeyStore,
        view: ExchangeView?,
        isTrade: Bool
    ) {
        self.output = TypedConsumer<ExchangeEvent>(output)
        self.system = system
        self.keyStore = keyStore
        self.isTrade = isTrade
        self.view = view
        super.init()
    }

    func accept(value: Any?) {
        switch value {
        case is ExchangeEffect.ExitFlow:
            exitFlow()
        case let effect as ExchangeEffect.ProcessUserAction:
            handleProcessUserAction(effect)
        case let effect as ExchangeEffect.TrackEvent:
            trackEvent(effect: effect)
        case let effect as ExchangeEffect.ErrorSignal:
            handleErrorSignal(effect)
        default:
            break
        }
    }

    private func exitFlow() {
        (view as? ExchangeBuySellViewController)?.exitFlow()
        (view as? ExchangeTradeViewController)?.exitFlow()
        (view as? RegionSettingsViewController)?.closeAction()
    }

    private func handleErrorSignal(_ effect: ExchangeEffect.ErrorSignal) {
        (view as? ExchangeBuySellViewController)?.errorSignalAction()
        (view as? ExchangeTradeViewController)?.errorSignalAction()
    }

    private func handleProcessUserAction(_ effect: ExchangeEffect.ProcessUserAction) {
        switch effect.action.type {
        case .browser:
            handleBrowserAction(effect)
        case .cryptoSend:
            handleCryptoSendAction(effect)
        default:
            break
        }
    }

    private func handleBrowserAction(_ effect: ExchangeEffect.ProcessUserAction) {
        guard let url = URL(string: "\(effect.baseUrl)\(effect.action.url)") else {
            return
        }

        let (navVC, browser) = WebViewController.embeddedInNavigationController(.brd)
        browser.title = effect.order.provider.name
        browser.flowEndUrlComponents = ["return", "return?"]
        browser.flowEndOnDidFinishNavigation = isSellOrder(effect.order)
        browser.load(URLRequest(url: url))
        browser.closeAction = {
            self.output.accept(.OnCloseClicked(confirmed: false))

            let alert = UIAlertController(
                title: S.Exchange.partnerWebViewAlertTitle,
                message: S.Exchange.partnerWebViewAlertBody,
                preferredStyle: .alert
            )

            let handler: (UIAlertAction) -> Void = { [weak self] _ in
                self?.output.accept(.OnCloseClicked(confirmed: true))
            }

            alert.addAction(.init(
                title: S.Exchange.partnerWebViewAlertOkay,
                style: .default,
                handler: handler
            ))

            alert.addAction(.init(
                title: S.Exchange.partnerWebViewAlertCancel,
                style: .cancel,
                handler: nil
            ))

            let vc = self.view?.navigationController?.visibleViewController
            vc?.present(alert, animated: true)
        }

        browser.flowEndedAction = { _ in
            let event = ExchangeEvent.OnBrowserActionCompleted(
                    action: effect.action,
                    cancelled: false
            )
            self.output.accept(event)
        }

        if #available(iOS 13.0, *) {
            navVC.isModalInPresentation = true
        }

        view?.present(navVC, animated: true)
    }

    private func trackEvent(effect: ExchangeEffect.TrackEvent) {
        saveEvent(effect.name, attributes: effect.props)
    }

    private func isSellOrder(_ order: ExchangeOrder?) -> Bool {
        guard let order = order else {
            return false
        }

        let fiatMedia: [ExchangeInput.Media] = [.card, .ach, .sepa]

        return !order.inputs.filter { $0.media == .crypto }.isEmpty  &&
            !order.outputs.filter { fiatMedia.contains($0.media) }.isEmpty
    }

    func dispose() { }
}

// MARK: - Crypto send

private extension NativeExchangeEffectHandler {

    func handleCryptoSendAction(_ effect: ExchangeEffect.ProcessUserAction) {
        let input = effect.order.inputs.first

        guard let transfer = input as? ExchangeInput.CryptoTransfer else {
            handleError(.loadInput, effect: effect)
            return
        }

        let currencyCode = transfer.currency.code
        let toAddress = transfer.sendToAddress
        let opCurrency = Store.state.currencies
            .filter { $0.code.lowercased() == currencyCode.lowercased() }
            .first

        guard let currency = opCurrency,
              let wallet = Store.state[currency]?.wallet,
              currency.isValidAddress(toAddress) else {
            handleError(.validateCurrencyOrAddress, effect: effect)
            return
        }

        let amount = Amount(
            tokenString: transfer.amount,
            currency: currency,
            locale: Locale(identifier: "en_US"),
            unit: currency.defaultUnit
        )

        estimateFeesAndMakeTransfer(
            wallet: wallet,
            toAddress: toAddress,
            amount: amount,
            currency: currency,
            effect: effect,
            attribute: transfer.sendToDestinationTag
        )
    }

    func estimateFeesAndMakeTransfer(
        wallet: Wallet,
        toAddress: String,
        amount: Amount,
        currency: Currency,
        effect: ExchangeEffect.ProcessUserAction,
        attribute: String?
    ) {
        let comment = comment(for: effect)
        wallet.estimateFee(
            address: toAddress,
            amount: amount,
            fee: .priority,
            isStake: false
        ) { [weak self] feeBasis in

            guard let transferFeeBasis = feeBasis else {
                self?.handleError(.noFeeBasis, effect: effect)
                return
            }

            self?.verifyAndMakeTransfer(
                wallet: wallet,
                toAddress: toAddress,
                amount: amount,
                currency: currency,
                effect: effect,
                feeBasis: transferFeeBasis,
                comment: comment,
                attribute: attribute
            )
        }
    }

    // swiftlint:disable:next function_parameter_count
    func verifyAndMakeTransfer(
        wallet: Wallet,
        toAddress: String,
        amount: Amount,
        currency: Currency,
        effect: ExchangeEffect.ProcessUserAction,
        feeBasis: TransferFeeBasis,
        comment: String?,
        attribute: String?
    ) {
        guard let kvStore = Backend.kvStore else {
            return
        }

        let sender = Sender(
            wallet: wallet,
            authenticator: keyStore,
            kvStore: kvStore
        )

        let result = sender.createTransaction(
            address: toAddress,
            amount: amount,
            feeBasis: feeBasis,
            comment: comment,
            attribute: attribute
        )

        guard case .ok = result else {
            if case .insufficientGas(let currencyCode, let amount) = result {
                DispatchQueue.main.async {
                    self.output.accept(ExchangeEvent.OnCryptoSendActionFailed(
                        reason: ExchangeEvent.SendFailedReasonInsufficientNativeWalletBalance(
                            currencyCode: currencyCode,
                            requiredAmount: amount.tokenValue.doubleValue
                        )
                    ))
                }
            } else {
                handleError(.transactionValidation(error: result), effect: effect)
            }
            return
        }

        let pinVerifier: PinVerifier = { [weak self] pinValidationCallback in
            let prompt = S.VerifyPin.authorize
            let handler: (PlatformAuthResult) -> Void = { [weak self] result in
                switch result {
                case .success(let pin?):
                    pinValidationCallback(pin)
                case .cancelled:
                    self?.handleCryptoSendCanceled(effect)
                default:
                    self?.handleError(.pinVerifier, effect: effect)
                }
            }
            Store.trigger(name: .authenticateForPlatform(prompt, false, handler))
        }

        let feeCurrency = sender.wallet.feeCurrency

        let confirmAmount = Amount(
            amount: amount,
            rate: nil,
            maximumFractionDigits: Amount.highPrecisionDigits
        )

        let feeAmount = Amount(
            cryptoAmount: feeBasis.fee,
            currency: feeCurrency,
            rate: nil,
            minimumFractionDigits: nil,
            maximumFractionDigits: Amount.highPrecisionDigits
        )

        let confirmationHandler: (Bool) -> Void = { [weak self] confirmed in
            guard confirmed else {
                self?.handleCryptoSendCanceled(effect)
                return
            }

            sender.sendTransaction(allowBiometrics: false, pinVerifier: pinVerifier) { result in
                switch result {
                case .success(let hash, _):
                    self?.output.accept(
                        .OnCryptoSendActionCompleted(
                            action: effect.action,
                            transactionHash: hash,
                            cancelled: false
                        )
                    )
                default:
                    self?.handleError(.sendResult(result: result), effect: effect)
                }
            }
        }

        Store.trigger(
            name: .confirmTransaction(
                currency,
                confirmAmount,
                feeAmount,
                .priority,
                toAddress,
                confirmationHandler
            )
        )
    }

    func comment(for effect: ExchangeEffect.ProcessUserAction) -> String? {
        guard let output = effect.order.outputs.first else {
            return nil
        }

        return "Sold for \(output.amount)\(output.currency.code.uppercased())."
    }

    func handleError(
        _ error: CryptoTransferError,
        effect: ExchangeEffect.ProcessUserAction
    ) {
        DispatchQueue.main.async {
            let alert = UIAlertController(
                title: "Error",
                message: error.errorDescription,
                preferredStyle: .alert
            )
            let retry = UIAlertAction(title: "Retry", style: .default) { [weak self] _ in
                self?.handleCryptoSendAction(effect)
            }
            let cancel = UIAlertAction(title: "Cancel", style: .cancel) { [weak self] _ in
                self?.handleCryptoSendCanceled(effect)
            }
            alert.addAction(cancel)
            alert.addAction(retry)
            self.view?.navigationController?.visibleViewController?.present(alert, animated: true)
        }
    }

    func handleCryptoSendCanceled(_ effect: ExchangeEffect.ProcessUserAction) {
        output.accept(
            .OnCryptoSendActionCompleted(
                action: effect.action,
                transactionHash: nil,
                cancelled: true
            )
        )
        output.accept(.OnBackClicked())
        output.accept(.OnCloseClicked(confirmed: true))
    }

    enum CryptoTransferError: Error, LocalizedError {
        case loadInput
        case validateCurrencyOrAddress
        case noFeeBasis
        case transactionValidation(error: SenderValidationResult)
        case pinVerifier
        case confirmation
        case sendResult(result: SendResult)
        case serviceUnavailable
        case serviceError
        case insufficientFunds

        var errorDescription: String? {
            switch self {
            case .loadInput:
                return S.Exchange.TransferError.loadInput
            case .validateCurrencyOrAddress:
                return S.Exchange.TransferError.validateCurrencyOrAddress
            case .noFeeBasis:
                return S.Exchange.TransferError.noFeeBasis
            case let .transactionValidation(error):
                return String(format: S.Exchange.TransferError.transactionValidation, "\(error)")
            case .pinVerifier:
                return S.Exchange.TransferError.pinVerifier
            case .confirmation:
                return S.Exchange.TransferError.confirmation
            case let .sendResult(result):
                return String(format: S.Exchange.TransferError.sendResult, "\(result)")
            case .serviceUnavailable:
                return S.Exchange.TransferError.serviceUnavailable
            case .serviceError:
                return S.Exchange.TransferError.serviceError
            case .insufficientFunds:
                return S.Exchange.TransferError.insufficientFunds

            }
        }

        static func from(_ error: WalletKit.Wallet.LimitEstimationError) -> CryptoTransferError {
            switch error {
            case .serviceUnavailable:
                return .serviceUnavailable
            case .serviceError:
                return .serviceError
            case .insufficientFunds:
                return .insufficientFunds
            }
        }
    }
}
