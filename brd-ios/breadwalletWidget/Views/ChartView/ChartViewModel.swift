//
//  ChartViewModel.swift
//  ChartDemo
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
import SwiftUI

struct ChartViewModel {
    let lineCandles: [Candle]
    let barCandles: [Candle]
    let lineChart: Bool
    let greenCandle: Color
    let redCandle: Color
    let colorOverride: Color?

    var chartColor: Color {
        if let color = colorOverride {
            return color
        }
        guard barCandles.count > 1 else {
            return greenCandle
        }
        let rising = barCandles[barCandles.count - 1].close >= barCandles[barCandles.count - 2].close
        return rising ? greenCandle : redCandle
    }

    func lineCandleViewModels(in size: CGSize) -> [ChartViewModel.Candle] {
        return ChartViewModel.normalized(lineCandles)
    }

    func barCandleViewModels(in size: CGSize) -> [ChartViewModel.BarCandle] {
        var viewModels: [ChartViewModel.BarCandle] = []
        guard size.width > 0, size.height > 0, !barCandles.isEmpty else {
            return []
        }

        let candles = self.barCandles.last(n: 30)
        let width = size.width / CGFloat(candles.count)
        let high = candles.sorted { $0.high > $1.high }.first?.high ?? 1
        let low = candles.sorted { $0.low < $1.low }.first?.low ?? 0
        let yLength = CGFloat(high - low)
        let yRatio = size.height / yLength

        for i in 0..<candles.count {
            let candle = candles[i]
            let (close, open) = (candle.close, candle.open)
            let isGreen = candle.close >= candle.open
            let bodyHigh = isGreen ? close : open
            let bodyLength = isGreen ? close - open : open - close

            viewModels.append(
                .init(
                    wick: .init(
                        origin: CGPoint(
                            x: CGFloat(i) * width + (width / 2 - 0.5),
                            y: (yLength - CGFloat(candle.high - low)) * yRatio
                        ),
                        size: CGSize(
                            width: 1,
                            height: CGFloat(candle.high - candle.low) * yRatio
                        )
                    ),
                    body: .init(
                        origin: CGPoint(
                            x: CGFloat(i) * width,
                            y: (yLength - CGFloat(bodyHigh - low)) * yRatio
                        ),
                        size: CGSize(
                            width: width,
                            height: CGFloat(bodyLength) * yRatio
                        )
                    ),
                    color: isGreen ? greenCandle : redCandle
                )
            )
        }
        return viewModels
    }
}

// MARK: - Candle

extension ChartViewModel {
    
    struct Candle {
        let open: Float
        let close: Float
        let high: Float
        let low: Float
        
        init(open: Float, close: Float, high: Float, low: Float) {
            self.open = open
            self.close = close
            self.high = high
            self.low = low
        }
    }
}

// MARK: - BarCandle

extension ChartViewModel {

    struct BarCandle {
        let wick: CGRect
        let body: CGRect
        let color: Color
    }
}

// MARK: - Default mock

extension ChartViewModel {
    
    static func mock(
        _ count: Int = 30,
        lineChart: Bool = true,
        greenCandle: Color = .green,
        redCandle: Color = .red
    ) -> ChartViewModel {
        guard let url = Bundle.main.url(forResource: "mock-candles", withExtension: "json") else {
            fatalError("mock-candles.json not found")
        }
        
        guard let data = try? Data(contentsOf: url) else {
            fatalError("could not load data from mock-candles.json")
        }
        
        guard let candles = try? JSONDecoder().decode([ChartViewModel.Candle].self, from: data) else {
            fatalError("could not decode data from mock-candles.json")
        }
        
        return .init(lineCandles: candles,
                     barCandles: candles,
                     lineChart: lineChart,
                     greenCandle: greenCandle,
                     redCandle: redCandle,
                     colorOverride: nil)
    }
    
    static func normalized(_ candles: [ChartViewModel.Candle]) -> [Candle] {
        let high = candles.sorted { $0.high > $1.high }.first?.high ?? 1
        let low = candles.sorted { $0.low < $1.low }.first?.low ?? 0
        let delta = high - low
        
        return candles.map {
            return .init(
                open: ($0.open - low) / delta,
                close: ($0.close - low) / delta,
                high: ($0.high - low) / delta,
                low: ($0.low - low) / delta
            )
        }
    }
}

// MARK: - ChartViewModel.Candle Decodable

extension ChartViewModel.Candle: Decodable {
    
    enum CodingKeys: String, CodingKey {
        case open
        case high
        case low
        case close
    }

    init(from decoder: Decoder) throws {
        let cont = try decoder.container(keyedBy: CodingKeys.self)
        open = try (try cont.decode(String.self, forKey: .open)).float()
        high = try (try cont.decode(String.self, forKey: .high)).float()
        low = try (try cont.decode(String.self, forKey: .low)).float()
        close = try (try cont.decode(String.self, forKey: .close)).float()
    }
}

// MARK: - MarketInfo.Candle

extension ChartViewModel.Candle {
    
    static func candles(_ candles: [Candle]) -> [ChartViewModel.Candle] {
        return candles.last(n: 90).map {
            .init(
                open: $0.open.float,
                close: $0.close.float,
                high: $0.high.float,
                low: $0.low.float
            )
        }
    }
}
