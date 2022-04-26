//
//  Configuration.swift
//  breadwallet
//
//  Created by stringcode on 11/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

struct Configuration {
    let assets: [AssetOption]
    let interval: IntervalOption
    let layout: WidgetListLayout
    let quoteCurrencyCode: String
    let style: StyleOption
    let textColor: ColorOption?
    let logoStyle: LogoStyle
    let backgroundColor: ColorOption?
    let showUpdatedTime: Bool
    let showSeparators: Bool
    let chartLocation: ChartLocation
    let chartStyle: ChartStyle
    let chartUpColor: ColorOption?
    let chartDownColor: ColorOption?
    let updated: Date

    var anyAsset: AssetOption {
        return assets[0] // TODO: Return mock for empty asset
    }
}

// MARK: - AssetIntent

extension Configuration {

    init(intent: AssetIntent, quoteCurrencyCode: String, updated: Date) {
        assets = [intent.asset].compactMap { $0 }
        interval = intent.interval
        layout = .standard
        self.quoteCurrencyCode = quoteCurrencyCode
        self.updated = updated
        style = intent.style
        textColor = intent.textColor
        logoStyle = intent.logoStyle
        backgroundColor = intent.backgroundColor
        showUpdatedTime = intent.showUpdatedTime?.boolValue ?? false
        showSeparators = false
        chartLocation = .middle
        chartStyle = intent.chartStyle
        chartUpColor = intent.chartUpColor
        chartDownColor = intent.chartDownColor
    }
}

// MARK: - AssetListIntent

extension Configuration {

    init(intent: AssetListIntent, quoteCurrencyCode: String, updated: Date) {
        assets = intent.assets ?? []
        interval = intent.interval
        layout = intent.layout
        self.quoteCurrencyCode = quoteCurrencyCode
        self.updated = updated
        style = .maxInfo
        textColor = intent.textColor
        logoStyle = intent.logoStyle
        backgroundColor = intent.backgroundColor
        showUpdatedTime = intent.showUpdatedTime?.boolValue ?? false
        showSeparators = intent.showSeparators?.boolValue ?? true
        chartLocation = intent.chartLocation
        chartStyle = intent.chartStyle
        chartUpColor = intent.chartUpColor
        chartDownColor = intent.chartDownColor
    }
}

// MARK: - PortfolioIntent

extension Configuration {

    init(intent: PortfolioIntent, quoteCurrencyCode: String, updated: Date) {
        assets = intent.assets ?? []
        interval = intent.interval
        layout = intent.layout
        self.quoteCurrencyCode = quoteCurrencyCode
        self.updated = updated
        style = .maxInfo
        textColor = intent.textColor
        logoStyle = intent.logoStyle
        backgroundColor = intent.backgroundColor
        showUpdatedTime = intent.showUpdatedTime?.boolValue ?? false
        showSeparators = intent.showSeparators?.boolValue ?? true
        chartLocation = intent.chartLocation
        chartStyle = intent.chartStyle
        chartUpColor = intent.chartUpColor
        chartDownColor = intent.chartDownColor
    }
}
