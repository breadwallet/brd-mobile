//
//  StringTests.swift
//  breadwalletTests
//
//  Created by Ehsan Rezaie on 2019-05-17.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import XCTest
@testable import breadwallet

class StringExtensionTests: XCTestCase {

    func testValidHexString() {
        XCTAssertTrue("".isValidHexString)
        XCTAssertTrue("0x".isValidHexString)
        XCTAssertFalse("0x0".isValidHexString)
        XCTAssertTrue("00".isValidHexString)
        XCTAssertTrue("0123456789ABCDEF".isValidHexString)
        XCTAssertFalse("123456789ABCDEF".isValidHexString)
        XCTAssertTrue("0xD224cA0c819e8E97ba0136B3b95ceFf503B79f53".isValidHexString)
        XCTAssertTrue("D224cA0c819e8E97ba0136B3b95ceFf503B79f53".isValidHexString)
        XCTAssertFalse("DEADBEEF!".isValidHexString)
        XCTAssertFalse("-12345".isValidHexString)
        XCTAssertFalse("0xHELLO".isValidHexString)
    }

    func testValidEmailAddress() {

    }

    func testConverters() {

    }
}
