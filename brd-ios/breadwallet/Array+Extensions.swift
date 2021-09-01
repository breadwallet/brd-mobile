// 
//  Array+Extensions.swift
//  breadwallet
//
//  Created by stringcode on 24/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import Cosmos

extension Array where Element: AnyObject {
    
    var kotlin: KotlinArray<Element> {
        KotlinArray<Element>(size: Int32(count)) { self[$0.intValue] }
    }
}

// MARK: -

extension Array {

    func last(n: Int) -> Array {
        guard count > n else {
            return self
        }
        return Array(self[count - n..<count])
    }

    func first(n: Int) -> Array {
        guard count > n else {
            return self
        }
        return Array(self[0..<n])
    }
}

// MARK: -

extension Array where Element: Equatable {

    func indexes(for elements: [Element?]) -> [Int] {
        elements.flatMap { indexes(for: $0) }
    }

    func indexes(for element: Element?) -> [Int] {
        guard let element = element else {
            return []
        }
        return enumerated()
            .filter { $0.1 == element }
            .map { $0.0 }
    }
}

// MARK: - Safe subscript

extension Collection {

    /// Returns the element at the specified index if it is within bounds, otherwise nil.
    subscript (safe index: Index) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
