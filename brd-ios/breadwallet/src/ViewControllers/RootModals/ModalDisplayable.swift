//
//  ModalDisplayable.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-12-01.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

protocol ModalDisplayable {
    var modalTitle: String { get }
    var faqArticleId: String? { get }
    var faqCurrency: Currency? { get }
}

protocol ModalPresentable {
    var parentView: UIView? { get set }
}
