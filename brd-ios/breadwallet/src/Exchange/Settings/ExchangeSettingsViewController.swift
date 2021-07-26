//
//  ExchangeSettingsViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

final class ExchangeSettingsViewController: UIViewController {

    private var flagView = UILabel()
    private var countryButton = CellLayoutButton(accessoryStyle: .large)
    private var regionButton = CellLayoutButton(accessoryStyle: .large)
    private var currencyButton = CellLayoutButton(accessoryStyle: .large)
    private var confirmButton = BRDButton(title: S.Exchange.Settings.applyRegion)
    private var closeAction: (() -> Void)?
    private var hideApplyButton: Bool = false

    private var model: ExchangeModel?
    private var consumer: TypedConsumer<ExchangeEvent>?

    init(_ hideApplyButton: Bool = false) {
        super.init(nibName: nil, bundle: nil)
        self.hideApplyButton = hideApplyButton
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setInitialData()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationItem.hidesBackButton = true
        navigationController?.navigationBar.prefersLargeTitles = true
        navigationItem.largeTitleDisplayMode = .never
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationItem.hidesBackButton = false
    }

    func update(with model: ExchangeModel, consumer: TypedConsumer<ExchangeEvent>?) {
        self.model = model
        self.consumer = consumer
        title = S.Exchange.Settings.title
        flagView.text = Locale.flagEmoji(model.selectedCountry?.code)
        countryButton.subtitle = model.selectedCountry?.name
        regionButton.subtitle = model.selectedRegion?.name
        currencyButton.subtitle = currencyString(model: model)
        confirmButton.isHidden = hideApplyButton
        regionButton.isHidden = model.selectedCountry?.regions.isEmpty ?? false
        countryButton.tap = { consumer?.accept(.OnConfigureCountryClicked()) }
        regionButton.tap = { consumer?.accept(.OnConfigureRegionClicked()) }
        currencyButton.tap = { consumer?.accept(.OnConfigureCurrencyClicked()) }
        confirmButton.tap = {
            let code = model.selectedFiatCurrency?.code ?? Store.state.defaultCurrencyCode
            Store.perform(action: DefaultCurrency.SetDefault(code))
            consumer?.accept(.OnContinueClicked())
        }
        closeAction = {
            let code = model.selectedFiatCurrency?.code ?? Store.state.defaultCurrencyCode
            Store.perform(action: DefaultCurrency.SetDefault(code))
            consumer?.accept(.OnCloseClicked(confirmed: false))
        }
        [countryButton, regionButton, currencyButton]
            .forEach { $0.setNeedsLayout() }
    }

    // TODO: Temporary work around until memory leak is fixed. Once done move to deinit
    func disconnect() {
        if hideApplyButton {
            let code = model?.selectedFiatCurrency?.code ?? Store.state.defaultCurrencyCode
            Store.perform(action: DefaultCurrency.SetDefault(code))
            consumer?.accept(.OnContinueClicked())
        }
    }
}

// MARK: - UISetup

private extension ExchangeSettingsViewController {

    func setupUI() {
        let flagView = UILabel()
        let stackView = VStackView([countryButton, regionButton, currencyButton])
        self.flagView = flagView

        [flagView, stackView, confirmButton]
            .forEach {
                $0.translatesAutoresizingMaskIntoConstraints = false
                view.addSubview($0)
        }

        NSLayoutConstraint.activate([
            flagView.topAnchor.constraint(equalTo: safeTopAnchor, constant: C.padding[3]),
            flagView.bottomAnchor.constraint(equalTo: stackView.topAnchor, constant: C.padding[-3]),
            flagView.centerXAnchor.constraint(equalTo: stackView.centerXAnchor),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: C.padding[3]),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: C.padding[-3]),
            confirmButton.leadingAnchor.constraint(equalTo: stackView.leadingAnchor),
            confirmButton.trailingAnchor.constraint(equalTo: stackView.trailingAnchor),
            confirmButton.bottomAnchor.constraint(equalTo: safeBottomAnchor, constant: C.padding[-1])
        ])

        stackView.spacing = C.padding[2]
        navigationItem.rightBarButtonItem = .close(
            self,
            action: #selector(closeHandler(_:))
        )
    }

    func setInitialData() {
        view.backgroundColor = Theme.quaternaryBackground
        flagView.font = UIFont.systemFont(ofSize: 144)
        [countryButton, regionButton, currencyButton].forEach {
            $0.titleStyle = .prominentOnEmptySubtitle
        }

        let arrow = UIImage(named: "RightArrow")
        countryButton.update(
            with: .init(title: S.Exchange.Settings.country, rightIconImage: arrow)
        )

        regionButton.update(
            with: .init(title: S.Exchange.Settings.region, rightIconImage: arrow)
        )

        currencyButton.update(
            with: .init(title: S.Exchange.Settings.currency, rightIconImage: arrow)
        )

        if let model = self.model {
            update(with: model, consumer: consumer)
        }
    }

    func currencyString(model: ExchangeModel) -> String {
        let code = model.selectedFiatCurrency?.code.uppercased() ?? "usd"
        let name = model.selectedFiatCurrency?.name ?? ""
        let symbol = Locale.currencySymbolByCode(code) ?? ""
        return "\(code.uppercased()) (\(symbol)) - \(name)"
    }

    @objc func closeHandler(_ sender: Any? = nil) {
        closeAction?()
    }
}
