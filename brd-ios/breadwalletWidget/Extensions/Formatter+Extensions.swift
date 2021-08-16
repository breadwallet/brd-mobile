// 
//  FormatterExtensions.swift
//  breadwalletWidgetExtension
//
//  Created by stringcode on 17/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

enum WidgetFormatter {
    
    static let price: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.isLenient = true
        formatter.numberStyle = .currency
        formatter.generatesDecimalNumbers = true
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()
    
    static let pctChange: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.locale = Locale.current
        formatter.isLenient = true
        formatter.numberStyle = .percent
        formatter.generatesDecimalNumbers = true
        formatter.positivePrefix = "+"
        formatter.negativePrefix = "-"
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()
    
    static let time: DateFormatter =  {
        let formatter = DateFormatter()
        formatter.dateStyle = .none
        formatter.timeStyle = .short
        return formatter
    }()
}

// MARK: - suffix number

extension Int {

    var abbreviated: String {
        let abbrev = ["K", "M", "B", "T", "P", "E"]
        return abbrev.enumerated()
            .reversed()
            .reduce(nil as String?) { accum, tuple in
                let factor = Double(self) / pow(10, Double(tuple.0 + 1) * 3)
                let truncRemainder = factor.truncatingRemainder(dividingBy: 1)
                let format = truncRemainder == 0 ? "%.0f%@" : "%.1f%@"

                if let accum = accum {
                    return accum
                }

                if factor > 1 {
                    return String(format: format, factor, String(tuple.1))
                }

                return nil
            } ?? String(self)
    }
}
