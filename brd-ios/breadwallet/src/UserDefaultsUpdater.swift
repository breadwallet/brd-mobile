//
//  UserDefaultsUpdater.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-05-27.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation

private enum AppGroup {
    #if TESTNET
    static let id = "group.com.brd.testnetQA"
    #elseif INTERNAL
    static let id = "group.com.brd.internalQA"
    #else
    static let id = "group.org.voisine.breadwallet"
    #endif
    static let requestDataKey = "kBRSharedContainerDataWalletRequestDataKey"
    static let receiveAddressKey = "kBRSharedContainerDataWalletReceiveAddressKey"
}
