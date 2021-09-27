//
//  RefreshInterval+Extensions.swift
//  breadwalletWidgetExtension
//
//  Created by blockexplorer on 11/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//	See the LICENSE file at the project root for license information.
//
	

import Foundation

extension RefreshInterval {
    
    func nextRefresh() -> Date {
        switch self {
        case .min15:
            return Date().adding(minutes: 15)
        case .min20:
            return Date().adding(minutes: 20)
        case .min60:
            return Date().adding(minutes: 60)
        default:
            return Date().adding(minutes: 30)
        }
    }
}
