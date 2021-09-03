// 
//  ExchangeViewConnection.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Cosmos
import UIKit
import WalletKit

// MARK: - ExchangeConnectable

class ExchangeConnectable: NSObject, Connectable {
    
    private let system: CoreSystem

    init(system: CoreSystem) {
        self.system = system
    }
    
    func connect(output: Consumer) -> Connection {
        return ExchangeEffectHandler(
            output: output,
            brdApi: Backend.brdApi,
            brdPrefs: UserDefaults.cosmos,
            walletProvider: IosWalletProvider(system: system),
            exchangeDataLoader: Backend.exchangeDataLoader
        )
    }
}

// MARK: - ExchangeViewConnectable

class ExchangeViewConnectable: NSObject, Connectable {

    let view: ExchangeView
    let system: CoreSystem?
    let keyStore: KeyStore

    init(
        view: ExchangeView,
        system: CoreSystem?,
        keyStore: KeyStore
    ) {
        self.view = view
        self.system = system
        self.keyStore = keyStore
    }

    func connect(output: Consumer) -> Connection {
        ExchangeViewConnection(
            output: output,
            view: view,
            system: system,
            keyStore: keyStore
        )
    }
}

// MARK: - ExchangeView

protocol ExchangeView: UIViewController {

    func update(with model: ExchangeModel?, consumer: TypedConsumer<ExchangeEvent>?)
    func errorSignalAction()
    func popToRoot()
    func closeAction()
}

// MARK: - ExchangeViewConnection

class ExchangeViewConnection: NSObject, Connection {

    let consumer: TypedConsumer<ExchangeEvent>
    let view: ExchangeView
    let system: CoreSystem?
    let keyStore: KeyStore

    init(
        output: Consumer,
        view: ExchangeView,
        system: CoreSystem?,
        keyStore: KeyStore
    ) {
        consumer = TypedConsumer<ExchangeEvent>(output)
        self.view = view
        self.system = system
        self.keyStore = keyStore
        super.init()
    }

    func accept(value: Any?) {
        guard let model = value as? ExchangeModel else {
            return
        }

        switch model.state {
        case is ExchangeModel.StateInitializing:
            // loading
            break
        case is ExchangeModel.StateConfigureSettings:
            handleStateConfigureSettings(model)
        case is ExchangeModel.StateEmptyWallets:
            handleEmptyWallets(model)
        case is ExchangeModel.StateOrderSetup:
            handleStateOrderSetup(model)
        case is ExchangeModel.StateSelectAsset:
            handleStateSelectCurrency(model)
        case is ExchangeModel.StateCreatingOrder:
            handleStateCreatingOrder(model)
        case is ExchangeModel.StateProcessingOrder:
            handleStateProcessingOrder(model)
        case is ExchangeModel.StateOrderComplete:
            handleStateOrderComplete(model)
        default:
            break
        }

        handleErrorState(model: model)
    }

    func dispose() { }
}

// MARK: - Event handlers

private extension ExchangeViewConnection {

    func handleStateInitializing(model: ExchangeModel) {
        if model.state as? ExchangeModel.StateInitializing == nil {
            return
        }

        view.update(with: model, consumer: consumer)
    }

    func handleStateConfigureSettings(_ model: ExchangeModel) {
        guard let state = model.state as? ExchangeModel.StateConfigureSettings else {
            return
        }
        switch state.target {
        case .menu:
            handleStateConfigureSettingsMenu(model)
        case .country:
            handleStateConfigureSettingsCountry(model)
        case .region:
            handleStateConfigureSettingsRegion(model)
        case .currency:
            handleStateConfigureSettingsCurrency(model)
        default:
            ()
        }
    }

    func handleStateConfigureSettingsMenu(_ model: ExchangeModel) {
        let state = model.state as? ExchangeModel.StateConfigureSettings

        if let vc = view(for: state?.target, settingsOnly: model.settingsOnly) as? ExchangeSettingsViewController {
            vc.update(with: model, consumer: consumer)
            view.update(with: model, consumer: consumer)
            vc.navigationController?.popToViewController(
                (view as? RegionSettingsViewController) ?? vc,
                animated: true
            )
            
            return
        }

        let vc = ExchangeSettingsViewController()
        vc.update(with: model, consumer: consumer)
        let navVC = UINavigationController(darkWith: vc)
        navVC.modalPresentationStyle = .overFullScreen
        view.present(navVC, animated: true)
    }

    func handleStateConfigureSettingsCountry(_ model: ExchangeModel) {
        let state = model.state as? ExchangeModel.StateConfigureSettings
        let viewModel = PickerViewModel(
            countryPickerFrom: model,
            consumer: consumer
        )

        guard !isPresentingPicker(for: state?.target, settingsOnly: model.settingsOnly) else {
            let currentVc = view(for: state?.target, settingsOnly: model.settingsOnly)
            (currentVc as? PickerViewController)?.update(with: viewModel)
            return
        }

        let vc = PickerViewController(viewModel: viewModel)
        view(for: .menu, settingsOnly: model.settingsOnly)?.show(vc, sender: self)
    }

    func handleStateConfigureSettingsRegion(_ model: ExchangeModel) {
        let state = model.state as? ExchangeModel.StateConfigureSettings
        let viewModel = PickerViewModel(
            regionPickerFrom: model,
            consumer: consumer
        )

        guard !isPresentingPicker(for: state?.target, settingsOnly: model.settingsOnly) else {
            let currentVc = view(for: state?.target, settingsOnly: model.settingsOnly)
            (currentVc as? PickerViewController)?.update(with: viewModel)
            return
        }

        let vc = PickerViewController(viewModel: viewModel)
        view(for: .menu, settingsOnly: model.settingsOnly)?.show(vc, sender: self)
    }

    func handleStateConfigureSettingsCurrency(_ model: ExchangeModel) {
        let state = model.state as? ExchangeModel.StateConfigureSettings

        let viewModel = PickerViewModel(
            fiatPickerFrom: model,
            consumer: consumer
        )

        guard !isPresentingPicker(for: state?.target, settingsOnly: model.settingsOnly) else {
            let currentVc = view(for: state?.target, settingsOnly: model.settingsOnly)
            (currentVc as? PickerViewController)?.update(with: viewModel)
            return
        }

        let vc = PickerViewController(viewModel: viewModel)
        view(for: .menu, settingsOnly: model.settingsOnly)?.show(vc, sender: self)
    }

    func handleEmptyWallets(_ model: ExchangeModel) {
        view.popToRoot()
        view.update(with: model, consumer: consumer)
    }

    func handleStateOrderSetup(_ model: ExchangeModel) {
        view.popToRoot()
        view.update(with: model, consumer: consumer)

        if (model.state as? ExchangeModel.StateOrderSetup)?.selectingOffer ?? false {
            handleStateSelectOffer(model)
        }
    }

    func handleStateSelectCurrency(_ model: ExchangeModel) {
        guard model.mode != .trade else {
            handleStateSelectCurrencyPair(model)
            return
        }

        guard let viewModel = PickerViewModel(
            currencyPickerFrom: model,
            assetCollection: system?.assetCollection,
            consumer: consumer
        ) else {
            return
        }

        let vc = PickerViewController(viewModel: viewModel)
        view.present(UINavigationController(darkWith: vc), animated: true)
    }

    func handleStateSelectCurrencyPair(_ model: ExchangeModel) {
        guard let viewModel = ExchangePairPickerViewModel(
            model,
            collection: system?.assetCollection,
            consumer: consumer
        ) else {
            return
        }

        let topVc = view.navigationController?.visibleViewController
        
        if let pickerVC = topVc as? ExchangePairPickerViewController {
            pickerVC.update(with: viewModel)
            return
        }

        let vc = ExchangePairPickerViewController(viewModel: viewModel)
        view.present(UINavigationController(darkWith: vc), animated: true)
    }

    func handleStateSelectOffer(_ model: ExchangeModel) {
        let viewModel = ExchangeOfferViewModel(model: model, consumer: consumer)
        let navVc = UINavigationController(
            darkWith: ExchangeOfferViewController(viewModel: viewModel),
            bgColor: Theme.quaternaryBackground
        )

        view.present(navVc, animated: true)
    }

    func handleStateCreatingOrder(_ model: ExchangeModel) {
        view.update(with: model, consumer: consumer)
        if (model.state as? ExchangeModel.StateCreatingOrder)?.previewing ?? false {
            let viewModel = ExchangeTradePreviewViewModel(
                model: model,
                assetCollection: system?.assetCollection,
                consumer: consumer,
                rewardsDetailAction: { [weak self] in self?.presentRewards() }
            )

            if let visibleVC = view
                .navigationController?
                .visibleViewController as? ExchangeTradePreviewViewController {
                    visibleVC.update(with: viewModel)
                    return
            }

            let vc = ExchangeTradePreviewViewController(viewModel: viewModel)
            view.show(vc, sender: self)
        }
    }

    func presentRewards() {
        Store.trigger(name: .presentRewardsInfo)
    }

    func handleStateProcessingOrder(_ model: ExchangeModel) {
        view.update(with: model, consumer: consumer)
        let topVc = view.navigationController?.topViewController

        guard let previewVc = topVc as? ExchangeTradePreviewViewController else {
            return
        }

        let viewModel = ExchangeTradePreviewViewModel(
            model: model,
            assetCollection: system?.assetCollection,
            consumer: consumer,
            rewardsDetailAction: { [weak self] in self?.presentRewards() }
        )

        previewVc.update(with: viewModel)
    }

    func handleStateOrderComplete(_ model: ExchangeModel) {
        let consumer = self.consumer
        let viewModel = ExchangeConfirmationViewModel(
            model: model,
            assetCollection: system?.assetCollection,
            doneAction: {
                self.view.closeAction()
                consumer.accept(.OnContinueClicked())
            }
        )

        guard exchangeOrderView() == nil else {
            exchangeOrderView()?.update(with: viewModel)
            return
        }

        if view.presentedViewController == nil {
            view.show(ExchangeConfirmationViewController(viewModel), sender: self)
            return
        }

        view.dismiss(animated: true) { [weak self] in
            self?.view.show(ExchangeConfirmationViewController(viewModel), sender: self)
        }
    }

    func exchangeOrderView() -> ExchangeConfirmationViewController? {
        let vc = view.navigationController?.topViewController
        return vc as? ExchangeConfirmationViewController
    }

    func isPresentingPicker(for target: ExchangeModel.ConfigTarget?, settingsOnly: Bool) -> Bool {
        return view(for: target, settingsOnly: settingsOnly) != nil
    }

    func view(
        for target: ExchangeModel.ConfigTarget?,
        settingsOnly: Bool
    ) -> UIViewController? {
        if target == .menu {
            if settingsOnly {
                return (view as? RegionSettingsViewController)?.settingsViewController
            }

            return (view.presentedViewController as? UINavigationController)?
                .viewControllers
                .first as? ExchangeSettingsViewController
        }
        return (view(for: .menu, settingsOnly: settingsOnly)?.navigationController?.viewControllers ?? [])
            .compactMap { $0 as? PickerViewController }
            .filter { $0.viewModel.id == target?.name ?? "Unknown" }
            .first
    }

    func handleErrorState(model: ExchangeModel) {
        guard let errorState = model.errorState else {
            return
        }

        let alert = UIAlertController(
            title: errorTitle(for: errorState),
            message: errorMessage(for: errorState),
            preferredStyle: .alert
        )

        var actions = [UIAlertAction]()

        if errorState.isRecoverable {
            actions = [
                .init(
                    title: S.Button.cancel,
                    style: .cancel,
                    handler: { [weak self] _ in
                        self?.consumer.accept(.OnDialogCancelClicked())
                    }
                ),
                .init(
                    title: errorConfirm(for: errorState),
                    style: .default,
                    handler: { [weak self] _ in
                        self?.consumer.accept(.OnDialogConfirmClicked())
                    }
                )
            ]
        } else {
            actions = [
                .init(
                    title: S.Button.ok,
                    style: .default,
                    handler: { [weak self] _ in
                        self?.consumer.accept(.OnDialogConfirmClicked())
                    }
                )
            ]
        }

        actions.forEach { alert.addAction($0) }
        view.present(alert, animated: true)
    }

    func errorMessage(for errorState: ExchangeModel.ErrorState) -> String {
        if let message = errorState.message {
            return message
        }
        switch errorState.type {
        case is ExchangeModel.ErrorStateTypeInitializationError:
            return S.Exchange.ErrorState.initialization
        case is ExchangeModel.ErrorStateTypeNetworkError:
            return S.Exchange.ErrorState.network
        case is ExchangeModel.ErrorStateTypeOrderError:
            return S.Exchange.ErrorState.order
        case is ExchangeModel.ErrorStateTypeUnknownError:
            return S.Exchange.ErrorState.unknown
        case is ExchangeModel.ErrorStateTypeTransactionError:
            let error = errorState as? ExchangeModel.ErrorStateTypeTransactionError
            let reason = error?.sendFailedReason
            if let uwReason =  reason as? ExchangeEvent.SendFailedReasonInsufficientNativeWalletBalance {
                return String(
                    format: S.Exchange.ErrorState.insufficientNativeWalletBalance,
                    uwReason.currencyCode,
                    uwReason.requiredAmount
                )
            } else if reason as? ExchangeEvent.SendFailedReasonCreateTransferFailed != nil{
                return S.Exchange.ErrorState.createTransferFailed
            } else if reason as? ExchangeEvent.SendFailedReasonFeeEstimateFailed  != nil {
                return S.Exchange.ErrorState.feeEstimateFailed
            }
            return S.Exchange.ErrorState.transaction
        case is ExchangeModel.ErrorStateTypeUnsupportedRegionError:
            return S.Exchange.ErrorState.unsupportedRegionError
        case is ExchangeModel.ErrorStateTypeInsufficientNativeBalanceError:
            return S.Exchange.ErrorState.insufficientNativeBalanceError
        default:
            return ""
        }
    }

    func errorConfirm(for errorState: ExchangeModel.ErrorState) -> String {
        switch errorState.type {
        case is ExchangeModel.ErrorStateTypeInsufficientNativeBalanceError:
            return S.Exchange.ErrorState.insufficientNativeBalanceErrorConfirm
        default:
            return S.Exchange.CTA.retry
        }
    }

    func errorTitle(for errorState: ExchangeModel.ErrorState) -> String {
        switch errorState.type {
        case is ExchangeModel.ErrorStateTypeInsufficientNativeBalanceError:
            return S.Exchange.ErrorState.insufficientNativeBalanceErrorTitle
        default:
            return errorState.title ?? S.Alert.error
        }
    }
}
