// 
//  File.swift
//  breadwallet
//
//  Created by stringcode on 17/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import CoinGecko

extension Resources {
        
    static func chart<MarketChart>(base: String, quote: String, interval: Interval, callback: @escaping Callback<MarketChart>) -> Resource<MarketChart> {
        
        var params = [URLQueryItem(name: "vs_currency", value: quote),
                      URLQueryItem(name: "days", value: interval.queryVal())]
        
        if interval == .daily {
            params.append(URLQueryItem(name: "interval", value: "daily"))
        }
        
        return Resource(.coinsMarketChart, method: .GET, pathParam: base, params: params, completion: callback)
    }

    static func candles(base: String, quote: String, interval: Interval, callback: @escaping Callback<CandleList>) -> Resource<CandleList> {
        let days = interval == .minute ? 1 : 30
        
        let candlesHandler: Callback<CandleList> = { result in
            switch result {
            case let .success(candles):
                let transformed = self.transform(candles: candles, for: interval)
                callback(.success(transformed))
            case let .failure(error):
                callback(.failure(error))
            }
        }
        
        return Resources.candles(currencyId: base, vs: quote, days: days, callback: candlesHandler)
    }

    private static func transform(candles: [Candle], for interval: Interval) -> CandleList {
        guard interval == .daily else {
            return candles
        }

        // Transforming 4h candles to daily
        let calendar = Calendar.current
        let grouped = Dictionary(grouping: candles, by: { candle -> String in
            let components = calendar.dateComponents([.day, .month], from: candle.date)
            return "\(components.month ?? 0)-\(components.day ?? 0)"
        })
        
        return grouped.values
            .map { $0 }
            .map { dayCandles in
                let sorted  = dayCandles.sorted { $0.date < $1.date }
                return Candle(
                    date: sorted.first?.date ?? Date(),
                    open: sorted.first?.open ?? 0,
                    high: sorted.sorted { $0.high > $1.high }.first?.high ?? 0,
                    low: sorted.sorted { $0.low < $1.low }.first?.low ?? 0,
                    close: sorted.last?.close ?? 0
                )
            }
            .sorted { $0.date < $1.date } as CandleList
    }

//    // Just to get it to compile until CoinGecko is updated
//    static func candles<CandleList>(currencyId: String, vs: String, days: Int, callback: @escaping Callback<CandleList>) -> Resource<CandleList> {
//        return Resource(.coinsMarketChart, method: .GET, pathParam: currencyId, params: [], completion: callback)
//    }
//
//    // Just to get it to compile until CoinGecko is updated
//    typealias CandleList = [Candle]
//
//    // Just to get it to compile until CoinGecko is updated
//    struct Candle: Codable {
//        let date: Date
//        let open: Double
//        let high: Double
//        let low: Double
//        let close: Double
//
//        init(date: Date, open: Double, high: Double, low: Double, close: Double) {
//            self.date = date
//            self.open = open
//            self.high = high
//            self.low = low
//            self.close = close
//        }
//
//        init(arrayData: [Double]) {
//            date = Date(timeIntervalSince1970: arrayData[0])
//            open = arrayData[1]
//            high = arrayData[2]
//            low = arrayData[3]
//            close = arrayData[4]
//        }
//    }
}

// MARK: - Interval

extension Resources {
    
    enum Interval {
        case daily
        case minute
        
        func queryVal() -> String {
            return self == .daily ? "max" : "1"
        }
    }
}

// MARK: - IntervalOption

extension IntervalOption {

    var resources: Resources.Interval {
        switch self {
        case .minute:
            return .minute
        default:
            return .daily
        }
    }
}
