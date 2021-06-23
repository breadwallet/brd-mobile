// 
//  ExAPIRequest.swift
//  breadwallet
//
//  Created by Jared Wheeler on 2/10/21.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

public protocol ExternalAPIRequest: Encodable {
    associatedtype Response: Decodable
    var hostName: String { get }
    var resourceName: String { get }
}
