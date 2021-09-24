//
//  SupportViewModel.swift
//  breadwallet
//
//  Created by blockexplorer on 06/07/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Cosmos
import UIKit

// MARK: - SupportConnectable

class SupportConnectable: NSObject, Connectable {
    
    func connect(output: Consumer) -> Connection {
        return SupportEffectHandler(
            output: output,
            supportDataProvider: IosSupportDataProvider()
        )
    }
}

// MARK: - SupportViewConnectable

class SupportViewConnectable: NSObject, Connectable {

    let view: SupportView

    init(view: SupportView) {
        self.view = view
    }

    func connect(output: Consumer) -> Connection {
        SupportViewConnection(output: output, view: view)
    }
}

// MARK: - SupportView

protocol SupportView: class {

    func update(with viewModel: SupportViewModel)
    func presentIndex(with viewModel: SupportViewModel)
    func presentSelectedArticle(with viewModel: SupportViewModel)
    func presentSelectedSection(with viewModel: SupportViewModel)
    func backAction()
    func closeAction()
}

// MARK: - SupportViewConnection

class SupportViewConnection: NSObject, Connection {

    let consumer: TypedConsumer<SupportEvent>
    let view: SupportView
    
    init(output: Consumer, view: SupportView) {
        consumer = TypedConsumer<SupportEvent>(output)
        self.view = view
        super.init()
    }

    func accept(value: Any?) {
        guard let model = value as? SupportModel else {
            return
        }
        print("==|| STATE", model.state)
        switch model.state {
        case is SupportModel.StateInitializing:
            handleStateInitializing(model)
        case is SupportModel.StateIndex:
            handleStateIndex(model)
        case is SupportModel.StateSection:
            handleStateSection(model)
        case is SupportModel.StateArticle:
            handleStateArticle(model)
        case is SupportModel.StateSearch:
            handleStateSearch(model)
        default:
            break
        }
        
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.update(with: viewModel)
    }

    func dispose() { }
}

// MARK: - Event handlers

private extension SupportViewConnection {
    
    func handleStateInitializing(_ model: SupportModel) {
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.update(with: viewModel)
    }

    func handleStateIndex(_ model: SupportModel) {
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.presentIndex(with: viewModel)
    }

    func handleStateSection(_ model: SupportModel) {
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.presentSelectedSection(with: viewModel)
    }

    func handleStateArticle(_ model: SupportModel) {
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.presentSelectedArticle(with: viewModel)
    }

    func handleStateSearch(_ model: SupportModel) {
        let viewModel = SupportViewModel(model: model, consumer: consumer)
        view.update(with: viewModel)
    }
}

// MARK: - NativeSupportConnectable

class NativeSupportConnectable: NSObject, Connectable {

    private weak var view: SupportView?

    init(view: SupportView?) {
        self.view = view
    }

    func connect(output: Consumer) -> Connection {
        NativeSupportEffectHandler(output: output, view: view)
    }
}

// MARK: - NativeSupportEffectHandler

class NativeSupportEffectHandler: NSObject, Connection, Trackable {

    private let output: TypedConsumer<SupportEvent>
    private weak var view: SupportView?

    init(output: Consumer, view: SupportView?) {
        self.output = TypedConsumer<SupportEvent>(output)
        self.view = view
        super.init()
    }

    func accept(value: Any?) {
        switch value {
        case is SupportEffect.ExitFlow:
            handleExitFlow()
        case let effect as SupportEffect.TrackEvent:
            handleTrackEvent(effect: effect)
        default:
            break
        }
    }

    private func handleExitFlow() {
        view?.closeAction()
    }

    private func handleTrackEvent(effect: SupportEffect.TrackEvent) {
        saveEvent(effect.name, attributes: effect.props)
    }

    func dispose() { }
}
