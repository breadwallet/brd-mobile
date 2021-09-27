//
//  String+Extensions.swift
//  ChartDemo
//
//  Created by stringcode on 11/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

// MARK: - Extracting number from string

extension String {

    func int() throws  -> Int {
        guard let num = Int(self) else {
            throw ParseError.failedToParseType(typeStr: "Int", fromStr: self)
        }
        return num
    }

    func double() throws  -> Double {
        guard let num = Double(self) else {
            throw ParseError.failedToParseType(typeStr: "Double", fromStr: self)
        }
        return num
    }
    
    func float() throws  -> Float {
        guard let num = Float(self) else {
            throw ParseError.failedToParseType(typeStr: "Float", fromStr: self)
        }
        return num
    }

    enum ParseError: Error {

        case failedToParseType(typeStr: String, fromStr: String)

        var errorDescription: String? {
            switch self {
            case let .failedToParseType(typeStr, fromStr):
                return "Failed to parse \(typeStr) out of \(fromStr)"
            }
        }
    }
}

extension String {

    func lastCharacterAsInt() -> Int? {
        guard !isEmpty else {
            return nil
        }
        return try? String(suffix(1)).int()
    }
}

// MARK: - fuzzy search

extension String {

    func fuzzyMatch(_ needle: String) -> Bool {
        if needle.isEmpty {
            return true
        }
        var remainder = Array(needle)
        for char in self {
            // swiftlint:disable:next for_where
            if char == remainder[remainder.startIndex] {
                remainder.removeFirst()
                if remainder.isEmpty { return true }
            }
        }
        return false
    }
}

// MARK: - sdbmhash constant seed hash

extension String {

    var sdbmhash: Int {
        let unicodeScalars = self.unicodeScalars.map { $0.value }
        return unicodeScalars.reduce(0) {
            (Int($1) &+ ($0 << 6) &+ ($0 << 16))
                .addingReportingOverflow(-$0)
                .partialValue
        }
    }
}

// MARK: - isEmpty

protocol EmptyTestable {
    var isEmpty: Bool { get }
}

extension Optional where Wrapped: EmptyTestable {
    /// `true` if the Wrapped value is `nil`,
    /// otherwise returns `wrapped.isEmpty`
    var isEmpty: Bool {
        switch self {
        case .some(let val):
            return val.isEmpty
        case .none:
            return true
        }
    }
}

extension String: EmptyTestable {}
