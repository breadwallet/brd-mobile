// 
//  Locale+Extension.swift
//  breadwallet
//
//  Created by stringcode on 27/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

extension Locale {
   
    var countryCode: String? {
        (Locale.current as NSLocale).object(forKey: .countryCode) as? String
    }
    
    static func currencySymbolByCode(_ currencyCode: String?) -> String? {
        guard let currencyCode = currencyCode?.uppercased() else {
            return nil
        }
        guard let locale = localeByCurrencyCode(currencyCode) else {
            print("locale for \(currencyCode) is nil")
            return nil
        }

        let symbol = locale.object(forKey: NSLocale.Key.currencySymbol) as? String
        return (symbol?.contains("$") ?? false) ? "$" : symbol
    }

    static func localeByCurrencyCode(_ currencyCode: String) -> NSLocale? {
        return NSLocale.availableLocaleIdentifiers
            .map { NSLocale(localeIdentifier: $0) }
            .filter { $0.object(forKey: .currencyCode) as? String == currencyCode }
            .first
    }

    static func flagEmoji(_ countryCode: String?) -> String? {
        guard let countryCode = countryCode else {
            return nil
        }
        return countryCode
            .uppercased()
            .unicodeScalars
            .map({ 127397 + $0.value })
            .compactMap(UnicodeScalar.init)
            .map(String.init)
            .joined()
    }
}
