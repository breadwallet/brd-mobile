//
//  File.swift
//  breadwallet
//
//  Created by blockexplorer on 15/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//	See the LICENSE file at the project root for license information.
//

import Foundation

// MARK: - CommonFormatter

enum CommonFormatter {

    static let price: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.isLenient = true
        formatter.numberStyle = .currency
        formatter.generatesDecimalNumbers = true
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    static let tmpCryptoBalance: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.isLenient = true
        formatter.generatesDecimalNumbers = true
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    static let backUpTradePrice: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.isLenient = true
        formatter.numberStyle = .currency
        formatter.currencyCode = "usd"
        formatter.generatesDecimalNumbers = true
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()
}

extension NumberFormatter {

    func string(from: Double) -> String? {
        return  self.string(from: NSNumber(value: from))
    }
}
