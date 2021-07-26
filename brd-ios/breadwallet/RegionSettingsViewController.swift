// 
//  File.swift
//  breadwallet
//
//  Created by blockexplorer on 30/06/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit
import Cosmos

class RegionSettingsViewController: CosmosViewController {

    private lazy var consumer: TypedConsumer<ExchangeEvent>? = {
        TypedConsumer<ExchangeEvent>(optional: self.consumer())
    }()

    let settingsViewController = ExchangeSettingsViewController(true)

    private let system: CoreSystem

    init(system: CoreSystem, keyStore: KeyStore) {
        self.system = system
        super.init(nibName: nil, bundle: nil)

        let isTest =  E.isDebug || E.isTestnet
        let factory = Mobius().loop(
            update: ExchangeUpdate(),
            effectHandler: CompositeEffectHandlerCompanion.from([
                ExchangeConnectable(system: system),
                NativeExchangeConnectable(
                    system: system,
                    keyStore: keyStore,
                    view: self
                )
            ])
        )
        .doInit(init: ExchangeInit())
        .logger(logger: Logger(tag: "Exchange"))
        .eventSource(eventSource: self)

        loopController = mobiusLoopController(
            loopFactory: factory,
            defaultModel: ExchangeModel.Companion().createForSettings(),
            view: ExchangeViewConnectable(view: self, system: system, keyStore: keyStore)
        )
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        let model = loopController?.model as? ExchangeModel
        update(with: model, consumer: consumer)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.navigationBar.barTintColor = Theme.quaternaryBackground
        navigationController?.navigationBar.shadowImage = UIImage()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.navigationBar.barTintColor = Theme.primaryBackground
        navigationController?.navigationBar.shadowImage = nil
    }

    override func didMove(toParent parent: UIViewController?) {
        super.didMove(toParent: parent)
        if parent == nil {
            // TODO: - Temporary work around until memory leak is fixed. Once done remove
            disconnect()
            settingsViewController.disconnect()
        }
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - ExchangeView

extension RegionSettingsViewController: ExchangeView {

    func update(with model: ExchangeModel?, consumer: TypedConsumer<ExchangeEvent>?) {
        guard let model = model, isViewLoaded else {
            return
        }

        let initializing = (model.state as? ExchangeModel.StateInitializing) != nil
        if !initializing && settingsViewController.view.alpha == 0 {
            UIView.animate(withDuration: C.animationDuration / 5) {
                self.settingsViewController.view.alpha = 1
            }
        }

        settingsViewController.update(with: model, consumer: consumer)
    }

    func errorSignalAction() { }

    func popToRoot() {
        navigationController?.popToRootViewController(animated: true)
    }

    func closeAction() {
        navigationController?.popToRootViewController(animated: true)
    }
}

private extension RegionSettingsViewController {

    func setupUI() {
        settingsViewController.willMove(toParent: self)
        addChild(settingsViewController)
        view.addSubview(settingsViewController.view)
        settingsViewController.view.constrain(toSuperviewEdges: nil)
        settingsViewController.view.alpha = 0
        title = S.Exchange.Settings.title
        navigationItem.largeTitleDisplayMode = .never
        view.backgroundColor = Theme.quaternaryBackground
    }

    typealias Logger = SimpleLogger<ExchangeModel, ExchangeEvent, ExchangeEffect>
}
