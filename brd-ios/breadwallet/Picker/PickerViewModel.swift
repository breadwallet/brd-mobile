// 
//  PickerViewModel.swift
//  breadwallet
//
//  Created by stringcode on 25/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit
import Cosmos

struct PickerViewModel {
    let id: String
    let title: String
    let titleDisplayMode: UINavigationItem.LargeTitleDisplayMode
    let searchEnabled: Bool
    let disableBackButton: Bool
    let prefersLargerCells: Bool
    let selectedIndexes: [Int]
    let items: [Item]
    let toolBarItems: [UIBarButtonItem]
    let backgroundColor: UIColor
    let sectionInsets: UIEdgeInsets
    let selectedAction: ((Int) -> Void)?
    let backAction: (() -> Void)?
    let closeAction: (() -> Void)?
}

// MARK: - Item

extension PickerViewModel {

    struct Item: Equatable {
        let title: String?
        let subtitle: String?
        let rightTitle: String?
        let rightSubtitle: String?
        let detail: String?
        let iconImage: UIImage?
        let iconText: String?
        let iconURL: URL?
    }
}

// MARK: - Convenience initializer

extension PickerViewModel {

    init(
        id: String = "",
        withTitle: String = "",
        titleDisplayMode: UINavigationItem.LargeTitleDisplayMode = .always,
        searchEnabled: Bool = true,
        disableBackButton: Bool = false,
        prefersLargerCells: Bool = false,
        selectedIndexes: [Int] = [],
        items: [Item] = [],
        toolBarItems: [UIBarButtonItem] = [],
        backgroundColor: UIColor = Theme.primaryBackground,
        sectionInsets: UIEdgeInsets = .zero,
        selectedAction: ((Int) -> Void)? = nil,
        backAction: (() -> Void)? = nil,
        closeAction: (() -> Void)? = nil
    ) {
        self.id = id
        self.title = withTitle
        self.titleDisplayMode = titleDisplayMode
        self.searchEnabled = searchEnabled
        self.disableBackButton = disableBackButton
        self.prefersLargerCells = prefersLargerCells
        self.selectedIndexes = selectedIndexes
        self.items = items
        self.toolBarItems = toolBarItems
        self.backgroundColor = backgroundColor
        self.sectionInsets = sectionInsets
        self.selectedAction = selectedAction
        self.backAction = backAction
        self.closeAction = closeAction
    }
}

// MARK: - Currency picker convenience initializer

extension PickerViewModel {

    init?(
        currencyPickerFrom model: ExchangeModel,
        assetCollection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?,
        showFiatRates: Bool = true,
        filterZeroBalances: Bool = false
    ) {
        guard let state = model.state as? ExchangeModel.StateSelectAsset else {
            return nil
        }

        let allCurrencies = assetCollection?.allAssets ?? [:]
        let currencies = state.assets
        let pair = model.selectedPair

        let buyFromCode = state.source ? pair?.fromCode : pair?.toCode
        let sellFromCode = state.source ? pair?.toCode : pair?.fromCode
        let selectedFromCode = model.mode == .sell ? sellFromCode : buyFromCode

        self.init(
            // TODO: Localize titles
            withTitle: model.mode == .sell ? "Sell" : state.source ? "Pay With" : "Assets",
            prefersLargerCells: true,
            selectedIndexes: [currencies
                .firstIndex(where: { $0.code == selectedFromCode ?? "" })]
                .compactMap { $0 },
            items: currencies
                .filter {
                    guard filterZeroBalances else {
                        return true
                    }
                    let balance = model.cryptoBalances[$0.code]?.doubleValue ?? 0
                    return balance != 0
                }
                .map {
                    let currencyId = CurrencyId(rawValue: $0.currencyId)
                    let balance = model.cryptoBalances[$0.code]?.doubleValue ?? 0
                    let prefix = showFiatRates ? "Balance " : ""
                    let balanceFormatted = prefix + (model.formattedCryptoBalances[$0.code] ?? "")
                    return PickerViewModel.Item(
                        withTitle: $0.code.uppercased(),
                        subtitle: $0.name,
                        rightTitle: showFiatRates
                            ? model.formattedFiatRates[$0.code]
                            : balance == 0 ? nil : balanceFormatted,
                        rightSubtitle: balance == 0 || !showFiatRates
                            ? nil
                            : balanceFormatted,
                        iconImage: allCurrencies[currencyId]?
                            .imageSquareBackground
                    )
                },
            selectedAction: { consumer?.accept(.OnCurrencyClicked(currency: currencies[$0])) },
            closeAction: { consumer?.accept(.OnBackClicked()) }
        )
    }
}

// MARK: - Country picker convenience initializer

extension PickerViewModel {

    init(
        countryPickerFrom model: ExchangeModel,
        consumer: TypedConsumer<ExchangeEvent>?
    ) {
        let state = model.state as? ExchangeModel.StateConfigureSettings
        let countryCode = Locale.current.countryCode?.lowercased() ?? ""
        let selectedCode = model.selectedCountry?.code ?? ""
        let sortedCountries = model.countries
            .sorted { (country, _) in  country.code == countryCode }
            .sorted { (country, _) in  country.code == selectedCode }

        self.init(
            id: state?.target.name ?? "",
            withTitle: S.Exchange.Settings.country,
            selectedIndexes: sortedCountries.indexes(for: model.selectedCountry),
            items: sortedCountries.map {
                PickerViewModel.Item(
                    withTitle: $0.name,
                    iconText: Locale.flagEmoji($0.code)
                )
            },
            sectionInsets: UIEdgeInsets(aTop: C.padding[1]),
            selectedAction: { consumer?.accept(.OnCountryClicked(country: sortedCountries[$0])) },
            backAction: { consumer?.accept(.OnBackClicked())},
            closeAction: { consumer?.accept(.OnCloseClicked(confirmed: true))}
        )
    }
}

// MARK: - Region picker convenience initializer

extension PickerViewModel {

    init(
        regionPickerFrom model: ExchangeModel,
        consumer: TypedConsumer<ExchangeEvent>?
    ) {
        let regions = model.selectedCountry?.regions ?? []
        let state = model.state as? ExchangeModel.StateConfigureSettings
        self.init(
            id: state?.target.name ?? "",
            withTitle: S.Exchange.Settings.state,
            selectedIndexes: regions.indexes(for: model.selectedRegion),
            items: regions.map { PickerViewModel.Item(withTitle: $0.name) },
            sectionInsets: UIEdgeInsets(aTop: C.padding[1]),
            selectedAction: { consumer?.accept(.OnRegionClicked(region: regions[$0])) },
            backAction: { consumer?.accept(.OnBackClicked()) },
            closeAction: { consumer?.accept(.OnCloseClicked(confirmed: true)) }
        )
    }
}

// MARK: - Fiat picker convenience initializer

extension PickerViewModel {

    init(
        fiatPickerFrom model: ExchangeModel,
        consumer: TypedConsumer<ExchangeEvent>?
    ) {
        let state = model.state as? ExchangeModel.StateConfigureSettings
        let currencies = state?.fiatCurrencies ?? []
        let selected = currencies.firstIndex(where: {
            $0.code == model.selectedFiatCurrency?.code ?? ""
        })
        
        self.init(
            id: state?.target.name ?? "",
            withTitle: S.Exchange.Settings.currency,
            selectedIndexes: [selected].compactMap { $0 },
            items: currencies.map {
                let symbol = Locale.currencySymbolByCode($0.code) ?? ""
                let name = "\($0.code.uppercased()) (\(symbol)) - \($0.name)"
                return PickerViewModel.Item(withTitle: name)
            },
            sectionInsets: UIEdgeInsets(aTop: C.padding[1]),
            selectedAction: { consumer?.accept(.OnCurrencyClicked(currency: currencies[$0])) },
            backAction: { consumer?.accept(.OnBackClicked()) },
            closeAction: { consumer?.accept(.OnCloseClicked(confirmed: true)) }
        )
    }
}

// MARK: - Convenience initializer

extension PickerViewModel.Item {

    init(
        withTitle: String? = nil,
        subtitle: String? = nil,
        rightTitle: String? = nil,
        rightSubtitle: String? = nil,
        detail: String? = nil,
        iconImage: UIImage? = nil,
        iconText: String? = nil,
        iconURL: URL? = nil
    ) {
        self.title = withTitle
        self.subtitle = subtitle
        self.rightTitle = rightTitle
        self.rightSubtitle = rightSubtitle
        self.detail = detail
        self.iconImage = iconImage
        self.iconText = iconText
        self.iconURL = iconURL
    }
}

// MARK: - Empty

extension PickerViewModel {

    static func empty() -> PickerViewModel {
        .init(
            withTitle: "",
            titleDisplayMode: .automatic,
            searchEnabled: false,
            selectedIndexes: [],
            items: [],
            toolBarItems: []
        )
    }
}

// MARK: - Mock

extension PickerViewModel.Item {

    static func mocks(
        count: Int = 20,
        titles: Bool = true,
        subtitles: Bool = false,
        details: Bool = false,
        images: Bool = false
    ) -> [PickerViewModel.Item] {
        (0..<count).map {
            PickerViewModel.Item.init(
                title: "Title \($0)",
                subtitle: subtitles ? "Subtitle \($0)" : nil,
                rightTitle: nil,
                rightSubtitle: nil,
                detail: subtitles ? "Some detail" : nil,
                iconImage: images ?  UIImage(named: "Document") : nil,
                iconText: nil,
                iconURL: nil
            )
        }
    }
}
