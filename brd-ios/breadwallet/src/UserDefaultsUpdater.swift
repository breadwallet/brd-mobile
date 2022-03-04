//
//  UserDefaultsUpdater.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-05-27.
//  Copyright © 2017-2019 Breadwinner AG. All rights reserved.
//

import Foundation

private enum AppGroup {
//    TODO: do we need this?
//    #if TESTNET
    static let id = "group.com.fabriik.one.testnetQA"
//    #elseif INTERNAL
//    static let id = "group.com.fabriik.one.internalQA"
//    #else
//    static let id = "group.com.fabriik.one"
//    #endif
    static let requestDataKey = "kBRSharedContainerDataWalletRequestDataKey"
    static let receiveAddressKey = "kBRSharedContainerDataWalletReceiveAddressKey"
}
