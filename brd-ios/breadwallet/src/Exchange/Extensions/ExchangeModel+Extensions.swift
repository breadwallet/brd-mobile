//
//  ExchangeModel+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos

extension ExchangeModel {

    static func create(mode: ExchangeModel.Mode, test: Bool) -> ExchangeModel {
        ExchangeModel.Companion().create(mode: mode, test: test)
    }

    func isInAny(of states: [ExchangeModel.State.Type]) -> Bool {
        for state in states {
            if self.state.isKind(of: state) {
                return true
            }
        }
        return false
    }
}

extension ExchangeModel.OfferDetails {

    func formattedViaMethod() -> String {
        switch offer.sourceCurrencyMethod {
        case is CurrencyMethod.Sepa:
            return S.Exchange.viaSEPA
        case is CurrencyMethod.Ach:
            return S.Exchange.viaACH
        case is CurrencyMethod.Card:
            return S.Exchange.viaCard
        case is CurrencyMethod.Crypto:
            return S.Exchange.viaCrypto
        default:
            return ""
        }
    }
}
