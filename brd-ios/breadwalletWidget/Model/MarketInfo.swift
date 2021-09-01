//
//  MarketInfo.swift
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
import CoinGecko

struct MarketInfo {
    let id: CurrencyId
    let price: Double
    let amount: Double?
    let marketCap: Double?
    let vol24hr: Double?
    let change24hr: Double?
    let lastUpdatedAt: Int?
    let lineCandles: [Candle]
    let barCandles: [Candle]
}

// MARK: - Convenience initializer

extension MarketInfo {

    init(id: CurrencyId,
         amount: Double?,
         simplePrice: SimplePrice,
         lineCandles: MarketChart?,
         barCandles: CandleList?) {
        self.id = id
        self.price = simplePrice.price
        self.amount = amount
        self.marketCap = simplePrice.marketCap
        self.vol24hr = simplePrice.vol24hr
        self.change24hr = simplePrice.change24hr
        self.lastUpdatedAt = simplePrice.lastUpdatedAt
        self.barCandles = (barCandles ?? []).last(n: 30)
        self.lineCandles = (lineCandles?.dataPoints.map {
             Candle(
                date: Date(timeIntervalSince1970: Double($0.timestamp) / 1000),
                open: $0.price,
                high: $0.price,
                low: $0.price,
                close: $0.price
            )
        } ?? barCandles ?? []).last(n: 90)
    }
}

// MARK: - Utilities

extension MarketInfo {
    
    var isChange24hrUp: Bool {
        return (change24hr ?? 0) < 0 ? false : true
    }
}
