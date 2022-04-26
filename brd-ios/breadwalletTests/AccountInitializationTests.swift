// 
//  AccountInitializationTests.swift
//  breadwalletTests
//
//  Created by Adrian Corscadden on 2020-04-15.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import XCTest
@testable import breadwallet

private var client: BRAPIClient?
private var keyStore: KeyStore!
private var system: CoreSystem!

class AccountInitializationTests : XCTestCase {

    override class func setUp() {
        super.setUp()
        clearKeychain()
        deleteKvStoreDb()
        keyStore = try! KeyStore.create()
        let account = setupNewAccount(keyStore: keyStore)
        Backend.connect(authenticator: keyStore)
        client = Backend.apiClient
        system = CoreSystem(keyStore: keyStore)
        system.create(account: account!, authToken: "", btcWalletCreationCallback: {}, completion: {})
    }
    
    override class func tearDown() {
        super.tearDown()
        system.shutdown(completion: nil)
        Backend.disconnectWallet()
        clearKeychain()
        keyStore.destroy()
    }
    
    func testInitializeHbar() {
        sleep(5) // NOTE: Workaround to stop `tearDown` being called right after `setup`
//        let exp = expectation(description: "Wallet initialization")
//        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//            print("[SYS] createAccount")
//            Store.trigger(name: .createAccount(TestCurrencies.hbar, { wallet in
//                XCTAssertNotNil(wallet, "Wallet should not be nil")
//                exp.fulfill()
//            }))
//        }
//        waitForExpectations(timeout: 60, handler: nil)
    }
    
}
