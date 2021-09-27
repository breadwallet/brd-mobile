// 
//  WidgetService.swift
//  breadwalletIntentHandler
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

typealias CurrencyHandler = (Result<[Currency], Error>) -> Void
typealias AssetOptionHandler = (Result<[AssetOption], Error>) -> Void
typealias ColorOptionHandler = (Result<[ColorOption], Error>) -> Void
typealias CurrencyInfoHandler = (CurrencyInfoResult) -> Void
typealias CurrencyInfoResult = Result<([Currency], [CurrencyId: MarketInfo]), Error>

protocol WidgetService {

    var lastRefreshed: Date { get }

    func fetchCurrenciesAndMarketInfo(for assetOptions: [AssetOption],
                                      quote: String,
                                      interval: IntervalOption,
                                      handler: @escaping CurrencyInfoHandler)

    func fetchCurrencies(handler: @escaping CurrencyHandler)
    func fetchAssetOptions(handler: @escaping AssetOptionHandler)
    func fetchBackgroundColorOptions(handler: ColorOptionHandler)
    func fetchTextColorOptions(handler: ColorOptionHandler)
    func fetchChartUpColorOptions(handler: ColorOptionHandler)
    func fetchChartDownColorOptions(handler: ColorOptionHandler)
    
    func defaultBackgroundColor() -> ColorOption
    func defaultTextColor() -> ColorOption
    func defaultChartUpOptions() -> ColorOption
    func defaultChartDownOptions() -> ColorOption
    func defaultAssetOptions() -> [AssetOption]
    func defaultCurrencies() throws -> [Currency]

    func quoteCurrencyCode() -> String
}

enum WidgetServiceError: Error {
    case failedToLoadCurrenciesFile
}

// MARK: - DefaultAssetsOptionsService

class DefaultWidgetService: WidgetService {

    let widgetDataShareService: WidgetDataShareService?
    let cacheService: CacheService?
    let imageStoreService: ImageStoreService?
    let coinGeckoClient = CoinGeckoClient()

    private(set) var currenciesCache: [Currency] = []
    private(set) var lastRefreshed: Date {
        didSet {
            cacheService?.cache(value: lastRefreshed, key: Constant.refreshedKey)
        }
    }

    init(
        widgetDataShareService: WidgetDataShareService?,
        imageStoreService: ImageStoreService?,
        cacheService: CacheService?
    ) {
        self.widgetDataShareService = widgetDataShareService
        self.imageStoreService = imageStoreService
        self.cacheService = cacheService
        self.lastRefreshed = cacheService?.cached(key: Constant.refreshedKey) ?? Date()
    }

    func fetchCurrenciesAndMarketInfo(for assetOptions: [AssetOption],
                                      quote: String,
                                      interval: IntervalOption,
                                      handler: @escaping CurrencyInfoHandler) {
        imageStoreService?.loadImagesIfNeeded()
        let uids = assetOptions.map { $0.identifier }
        fetchCurrencies { [weak self] result in
            switch result {
            case let .success(currencies):
                let selected = currencies.filter { uids.contains($0.uid.rawValue) }
                self?.fetchPriceListAndChart(for: selected,
                                             quote: quote,
                                             interval: interval,
                                             handler: handler)
            case let .failure(error):
                handler(.failure(error))
            }
        }
    }

    func fetchCurrencies(handler: @escaping CurrencyHandler) {
        if !currenciesCache.isEmpty {
            handler(.success(currenciesCache))
            return
        }
        do {
            let currencies = try defaultCurrencies()
            handler(.success(currencies))
        } catch {
            handler(.failure(error))
        }
    }

    func fetchAssetOptions(handler: @escaping AssetOptionHandler) {
        fetchCurrencies { result in
            switch result {
            case let .success(currencies):
                handler(.success(currencies.map { $0.assetOption() }))
            case let .failure(error):
                handler(.failure(error))
            }
        }
    }
    
    func defaultAssetOptions() -> [AssetOption] {
        return ((try? defaultCurrencies()) ?? [])
            .filter { Constant.defaultCurrencyCodes.contains($0.code.uppercased()) }
            .map { $0.assetOption() }
    }

    func fetchBackgroundColorOptions(handler: ColorOptionHandler) {
        let currenciesColors = ((try? defaultCurrencies()) ?? []).map {
            ColorOption(currency: $0)
        }
        
        let colorOptions = [ColorOption.autoBackground]
            + ColorOption.backgroundColors()
            + ColorOption.basicColors()
            + currenciesColors

        handler(.success(colorOptions))
    }

    func defaultBackgroundColor() -> ColorOption {
        return ColorOption.autoBackground
    }
    
    func fetchTextColorOptions(handler: ColorOptionHandler) {
        let colorOptions = [ColorOption.autoTextColor]
            + ColorOption.textColors()
            + ColorOption.basicColors()
            + ColorOption.backgroundColors()
        handler(.success(colorOptions))
    }

    func defaultTextColor() -> ColorOption {
        return ColorOption.autoTextColor
    }

    func fetchChartUpColorOptions(handler: ColorOptionHandler) {
        let options = ColorOption.basicColors()
            + ColorOption.textColors()
            + ColorOption.backgroundColors()
        handler(.success(options))
    }

    func defaultChartUpOptions() -> ColorOption {
        return ColorOption.green
    }

    func fetchChartDownColorOptions(handler: ColorOptionHandler) {
        let options = ColorOption.basicColors()
            + ColorOption.textColors()
            + ColorOption.backgroundColors()
        handler(.success(options))
    }

    func defaultChartDownOptions() -> ColorOption {
        return ColorOption.red
    }

    func defaultCurrencies() throws -> [Currency] {
        guard currenciesCache.isEmpty else {
            return currenciesCache
        }
        guard let url = Constant.currenciesURL else {
            throw WidgetServiceError.failedToLoadCurrenciesFile
        }

        do {
            let data = try Data(contentsOf: url)
            let decoder = JSONDecoder()
            let meta = try decoder.decode([CurrencyMetaData].self, from: data)
            let currencies = meta
                .map { Currency(metaData: $0)}
                .filter { $0.isSupported }
                .sorted { $0.metaData.isPreferred && !$1.metaData.isPreferred }
                .sorted { $0.isEthereum && !$1.isEthereum }
                .sorted { $0.isBitcoin && !$1.isBitcoin }
            currenciesCache = currencies
            return currencies
        }
    }
    
    func quoteCurrencyCode() -> String {
        return widgetDataShareService?.quoteCurrencyCode ?? "USD"
    }
}

// MARK: - Loading chart and simple price

private extension DefaultWidgetService {

    typealias MarketChartResult = Result<MarketChart, CoinGeckoError>
    typealias CandleListResult = Result<CandleList, CoinGeckoError>
    typealias PriceListResult = Result<PriceList, CoinGeckoError>

    func fetchPriceListAndChart(for currencies: [Currency],
                                quote: String,
                                interval: IntervalOption,
                                handler: @escaping (CurrencyInfoResult) -> Void) {
        let group = DispatchGroup()
        let rInterval = interval.resources
        var priceList: PriceList = []
        var candleLineInfo: [CurrencyId: MarketChart] = [:]
        var candleBarInfo: [CurrencyId: CandleList] = [:]
        var error: Error?
        let codes = currencies.compactMap { $0.coinGeckoId }

        group.enter()
        self.priceList(codes: codes, base: quote) { result in
            switch result {
            case let .success(list):
                self.cache(list, codes: codes, quote: quote)
                self.lastRefreshed = Date()
                priceList = list
            case let .failure(err):
                let cache = self.cachedPriceList(codes: codes, quote: quote)
                if !cache.isEmpty {
                    priceList = cache
                } else {
                    error = err
                }
            }
            group.leave()
        }

        for currency in currencies {
            guard let code = currency.coinGeckoId else {
                continue
            }
            group.enter()
            group.enter()

            chart(code: code, quote: quote, interval: rInterval) { result in
                switch result {
                case let .success(candleList):
                    candleLineInfo[currency.uid] = candleList
                case let .failure(error):
                    print(error)
                }
                group.leave()
            }

            candles(code: code, quote: quote, interval: rInterval) { result in
                switch result {
                case let .success(candleList):
                    candleBarInfo[currency.uid] = candleList
                case let .failure(error):
                    print(error)
                }
                group.leave()
            }
        }
        
        group.notify(queue: DispatchQueue.global()) {
            if let error = error {
                handler(.failure(error))
                return
            }
            if candleLineInfo.isEmpty {
                candleLineInfo = self.cachedLineCandles(codes: codes, quote: quote, interval: rInterval)
            } else {
                self.cacheLine(candles: candleLineInfo, codes: codes, quote: quote, interval: rInterval)
            }
            if candleBarInfo.isEmpty {
                candleBarInfo = self.cachedBarCandles(codes: codes, quote: quote, interval: rInterval)
            } else {
                self.cacheBar(candles: candleBarInfo, codes: codes, quote: quote, interval: rInterval)
            }
            let info = self.marketInfo(
                currencies,
                priceList: priceList,
                candlesLine: candleLineInfo,
                candlesBar: candleBarInfo
            )
            handler(.success((currencies, info)))
        }
    }

    func priceList(codes: [String], base: String, handler: @escaping (PriceListResult) -> Void) {
        let prices = Resources.simplePrice(ids: codes,
                                           vsCurrency: base,
                                           options: SimplePriceOptions.allCases,
                                           handler)
        coinGeckoClient.load(prices)
    }
    
    func chart(code: String,
               quote: String,
               interval: Resources.Interval,
               handler: @escaping (MarketChartResult) -> Void) {

        let chart = Resources.chart(base: code,
                                    quote: quote,
                                    interval: interval,
                                    callback: handler)
        coinGeckoClient.load(chart)
    }

    func candles(code: String,
                 quote: String,
                 interval: Resources.Interval,
                 handler: @escaping (CandleListResult) -> Void) {

        let candles = Resources.candles(base: code,
                                        quote: quote,
                                        interval: interval,
                                        callback: handler)        
        coinGeckoClient.load(candles)
    }

    func marketInfo(_ currencies: [Currency],
                    priceList: PriceList,
                    candlesLine: [CurrencyId: MarketChart],
                    candlesBar: [CurrencyId: CandleList]) -> [CurrencyId: MarketInfo] {

        var result = [CurrencyId: MarketInfo]()
        
        for currency in currencies {
            let coinGeckoId = currency.coinGeckoId ?? "error"
            let simplePrice = priceList.first(where: { $0.id == coinGeckoId})
            let amount = widgetDataShareService?.amount(for: currency.uid)

            if let price = simplePrice {
                result[currency.uid] = MarketInfo(id: currency.uid,
                                                  amount: amount,
                                                  simplePrice: price,
                                                  lineCandles: candlesLine[currency.uid],
                                                  barCandles: candlesBar[currency.uid])
            }
        }
        return result
    }
}

// MARK: - Cache handling

private extension DefaultWidgetService {

    func cache(_ priceList: PriceList, codes: [String], quote: String) {
        let key = cacheKey(codes: codes, quote: quote)
        cacheService?.cache(value: priceList, key: key)
    }

    func cachedPriceList(codes: [String], quote: String) -> PriceList {
        let key = cacheKey(codes: codes, quote: quote)
        let priceList: PriceList = cacheService?.cached(key: key) ?? []
        return priceList
    }

    func cacheLine(
        candles: [CurrencyId: MarketChart],
        codes: [String],
        quote: String,
        interval: Resources.Interval
    ) {
        let key = lineCandleKey(codes: codes, quote: quote, interval: interval)
        cacheService?.cache(value: candles, key: key)
    }

    func cacheBar(
        candles: [CurrencyId: CandleList],
        codes: [String],
        quote: String,
        interval: Resources.Interval
    ) {
        let key = barCandleKey(codes: codes, quote: quote, interval: interval)
        cacheService?.cache(value: candles, key: key)
    }

    func cachedLineCandles(
        codes: [String],
        quote: String,
        interval: Resources.Interval) -> [CurrencyId: MarketChart] {
        let key = lineCandleKey(codes: codes, quote: quote, interval: interval)
        let candles: [CurrencyId: MarketChart] = cacheService?.cached(key: key) ?? [:]
        return candles
    }

    func cachedBarCandles(
        codes: [String],
        quote: String,
        interval: Resources.Interval) -> [CurrencyId: CandleList] {
        let key = barCandleKey(codes: codes, quote: quote, interval: interval)
        let candles: [CurrencyId: CandleList] = cacheService?.cached(key: key) ?? [:]
        return candles
    }

    func cacheKey(codes: [String], quote: String) -> String {
        "\(Constant.cachePriceListKey)-\(codes.reduce("", +))-\(quote)"
    }

    func lineCandleKey(
        codes: [String],
        quote: String,
        interval: Resources.Interval
    ) -> String {
        "\(Constant.cacheLineKey)-\(codes.reduce("", +))-\(quote)-\(interval)"
    }

    func barCandleKey(
        codes: [String],
        quote: String,
        interval: Resources.Interval
    ) -> String {
        "\(Constant.cacheBarKey)-\(codes.reduce("", +))-\(quote)-\(interval)"
    }
}

// MARK: - Utilities

private extension DefaultWidgetService {

    enum Constant {
        static let cachePriceListKey = "cachePriceList"
        static let cacheLineKey = "cacheCandleLine"
        static let cacheBarKey = "cacheCandleBar"
        static let refreshedKey = "refreshed"
        static let defaultCurrencyCodes = ["BTC", "ETH", "BRD"]
        static let currenciesURL = Bundle.main.url(forResource: "currencies",
                                                   withExtension: "json")

    }
}
