// 
//  PaymentPath.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2020-04-28.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import Cosmos

extension AddressType {
    var label: String {
        switch self {
        case is AddressType.ResolvablePayId:
            return "PayString"
        case is AddressType.ResolvableFio:
            return "FIO"
        case is AddressType.ResolvableUnstoppableDomainENS:
            return "ENS"
        default:
            return "Unstoppable"
        }
    }

    var iconName: String {
        switch self {
        case is AddressType.ResolvablePayId:
            return "payidIcon"
        case is AddressType.ResolvableFio:
            return "fioIcon"
        case is AddressType.ResolvableUnstoppableDomainENS:
            return "ensIcon"
        default:
            return "udomainIcon"
        }
    }
}

struct ResolvedAddress {
    let humanReadableAddress: String
    let cryptoAddress: String
    let label: String
    let type: AddressType
}
