//
//  ColorOption+Extension.swift
//  breadwalletWidgetExtension
//
//  Created by stringcode on 15/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//
import Foundation
import SwiftUI

extension ColorOption {

    var isSystem: Bool {
        return identifier?.contains(Constant.systemPrefix) ?? false
    }

    var isBrdBackgroundColor: Bool {
        return identifier?.contains(Constant.brdBgPrefix) ?? false
    }

    var isBrdTextColor: Bool {
        return identifier?.contains(Constant.brdTextPrefix) ?? false
    }

    var isCurrencyColor: Bool {
        return identifier?.contains(Constant.currencyPrefix) ?? false
    }

    var isBasicColor: Bool {
        return !isSystem && !isCurrencyColor && !isBrdBackgroundColor && !isBrdTextColor
    }
    
    var identifierWithoutPrefix: String? {
        identifier?
            .replacingOccurrences(of: Constant.systemPrefix, with: "")
            .replacingOccurrences(of: Constant.currencyPrefix, with: "")
            .replacingOccurrences(of: Constant.brdTextPrefix, with: "")
            .replacingOccurrences(of: Constant.brdBgPrefix, with: "")
    }
}

// MARK: - Environment / System based ColorOptions

extension ColorOption {

    static let autoBackground = ColorOption(
        identifier: Constant.systemBgId,
        display: S.Widget.Color.autoLightDark
    )
    
    static let autoTextColor = ColorOption(
        identifier: Constant.systemTextId,
        display: S.Widget.Color.autoLightDark
    )
}

// MARK: - Convenience initializer

extension ColorOption {
    
    convenience init(currency: Currency) {
        self.init(identifier: Constant.currencyPrefix + currency.uid.rawValue,
                  display: currency.name)
    }
}

// MARK: - Basic colors

extension ColorOption {

    static let white = ColorOption(identifier: ".white", display: S.Widget.Color.white)
    static let black = ColorOption(identifier: ".black", display: S.Widget.Color.black)
    static let gray = ColorOption(identifier: ".gray", display: S.Widget.Color.gray)
    static let grayDark = ColorOption(identifier: ".grayDark", display: S.Widget.Color.grayDark)
    static let grayLight = ColorOption(identifier: ".grayLight", display: S.Widget.Color.grayLight)
    static let red = ColorOption(identifier: ".red", display: S.Widget.Color.red)
    static let redAlt = ColorOption(identifier: ".redAlt", display: S.Widget.Color.redAlt)
    static let redDark = ColorOption(identifier: ".redDark", display: S.Widget.Color.redDark)
    static let redLight = ColorOption(identifier: ".redLight", display: S.Widget.Color.redLight)
    static let green = ColorOption(identifier: ".green", display: S.Widget.Color.green)
    static let greenAlt = ColorOption(identifier: ".greenAlt", display: S.Widget.Color.greenAlt)
    static let greenDark = ColorOption(identifier: ".greenDark", display: S.Widget.Color.greenDark)
    static let greenLight = ColorOption(identifier: ".greenLight", display: S.Widget.Color.greenLight)
    static let blue = ColorOption(identifier: ".blue", display: S.Widget.Color.blue)
    static let blueDark = ColorOption(identifier: ".blueDark", display: S.Widget.Color.blueDark)
    static let blueLight = ColorOption(identifier: ".blueLight", display: S.Widget.Color.blueLight)
    static let orange = ColorOption(identifier: ".orange", display: S.Widget.Color.orange)
    static let orangeDark = ColorOption(identifier: ".orangeDark", display: S.Widget.Color.orangeDark)
    static let orangeLight = ColorOption(identifier: ".orangeLight", display: S.Widget.Color.orangeLight)
    static let yellow = ColorOption(identifier: ".yellow", display: S.Widget.Color.yellow)
    static let yellowDark = ColorOption(identifier: ".yellowDark", display: S.Widget.Color.yellowDark)
    static let yellowLight = ColorOption(identifier: ".yellowLight", display: S.Widget.Color.yellowLight)
    static let pink = ColorOption(identifier: ".pink", display: S.Widget.Color.pink)
    static let pinkDark = ColorOption(identifier: ".pinkDark", display: S.Widget.Color.pinkDark)
    static let pinkLight = ColorOption(identifier: ".pinkLight", display: S.Widget.Color.pinkLight)
    static let purple = ColorOption(identifier: ".purple", display: S.Widget.Color.purple)
    static let purpleDark = ColorOption(identifier: ".purpleDark", display: S.Widget.Color.purpleDark)
    static let purpleLight = ColorOption(identifier: ".purpleLight", display: S.Widget.Color.purpleLight)

    static func basicColors() -> [ColorOption] {
        return [
            .white, .black, .gray, .grayDark, .grayLight, .red, .redAlt, .redDark,
            .redLight, .green, .greenAlt, .greenDark, .greenLight, .blue, .blueDark,
            .blueLight, .orange, .orangeDark, .orangeLight, .yellow, .yellowDark,
            .yellowLight, .pink, .pinkDark, .pinkLight, .purple, .purpleDark,
            .purpleLight
        ]
    }
}

// MARK: - Background colors

extension ColorOption {

    static let primaryBackground = ColorOption(identifier: Constant.brdBgPrefix + "primaryBackground",
                                               display: S.Widget.Color.primaryBackground)
    static let secondaryBackground = ColorOption(identifier: Constant.brdBgPrefix + "secondaryBackground",
                                               display: S.Widget.Color.secondaryBackground)
    static let tertiaryBackground = ColorOption(identifier: Constant.brdBgPrefix + "tertiaryBackground",
                                                display: S.Widget.Color.tertiaryBackground)

    static func backgroundColors() -> [ColorOption] {
        return [.primaryBackground, secondaryBackground, .tertiaryBackground]
    }
}

// MARK: - Text colors

extension ColorOption {

    static let primaryText = ColorOption(identifier: Constant.brdTextPrefix + "primaryText",
                                         display: S.Widget.Color.primaryText)
    static let secondaryText = ColorOption(identifier: Constant.brdTextPrefix + "secondaryText",
                                           display: S.Widget.Color.secondaryText)
    static let tertiaryText = ColorOption(identifier: Constant.brdTextPrefix + "tertiaryText",
                                          display: S.Widget.Color.tertiaryText)

    static func textColors() -> [ColorOption] {
        return [.primaryText, secondaryText, .tertiaryText]
    }
}

// MARK: - Utilities

extension ColorOption {
    
    func colors(in colorScheme: ColorScheme? = nil, currencies: [Currency] = []) -> [Color] {
        if isCurrencyColor {
            let id = identifierWithoutPrefix
            return currencies
                .filter { $0.uid.rawValue == id }
                .map { [Color($0.colors.0), Color($0.colors.1)] }
                .first ?? []
        }
        if isBasicColor || isBrdTextColor || isBrdBackgroundColor {
            return [color(in: colorScheme), color(in: colorScheme)]
                .compactMap { $0 }
        }
        return []
    }
    
    func color(in colorScheme: ColorScheme? = nil) -> Color? {
        switch self {
        case .white:
            return .white
        case .black:
            return .black
        case .gray:
            return .gray
        case .grayDark:
            return Color(UIColor.gray.darker())
        case .grayLight:
            return Color(UIColor.gray.lighter())
        case .red:
            return .red
        case .redAlt:
            return Color(UIColor.fromHex("#EF5350"))
        case .redDark:
            return Color(UIColor.red.darker())
        case .redLight:
            return Color(UIColor.red.lighter())
        case .green:
            return .green
        case .greenAlt:
            return Color(UIColor.fromHex("#25A69A"))
        case .greenDark:
            return Color(UIColor.green.darker())
        case .greenLight:
            return Color(UIColor.green.lighter())
        case .blue:
            return .blue
        case .blueDark:
            return Color(UIColor.blue.darker())
        case .blueLight:
            return Color(UIColor.blue.lighter())
        case .orange:
            return .orange
        case .orangeDark:
            return Color(UIColor.orange.darker())
        case .orangeLight:
            return Color(UIColor.orange.lighter())
        case .yellow:
            return .yellow
        case .yellowDark:
            return Color(UIColor.yellow.darker())
        case .yellowLight:
            return Color(UIColor.yellow.lighter())
        case .pink:
            return .pink
        case .pinkDark:
            return Color(UIColor.pink.darker())
        case .pinkLight:
            return Color(UIColor.pink.lighter())
        case .purple:
            return .purple
        case .purpleDark:
            return Color(UIColor.purple.darker())
        case .purpleLight:
            return Color(UIColor.purple.lighter())
        case .primaryBackground:
            return Color(Theme.primaryBackground)
        case .secondaryBackground:
            return Color(Theme.secondaryBackground)
        case .tertiaryBackground:
            return Color(Theme.tertiaryBackground)
        case .primaryText:
            return Color(Theme.primaryText)
        case .secondaryText:
            return Color(Theme.secondaryText)
        case .tertiaryText:
            return Color(Theme.tertiaryText)
        default:
            return nil
        }
    }
}

// MARK: - Constants

private extension ColorOption {

    enum Constant {
        static let currencyPrefix = "currency-"
        static let systemPrefix = "system-"
        static let brdTextPrefix = "brdtext-"
        static let brdBgPrefix = "brdbg-"
        static let systemBgId = Constant.systemPrefix + "bg"
        static let systemTextId = Constant.systemPrefix + "text"
    }
}
