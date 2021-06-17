//
//  CutoutLabel.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2018-06-15.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class CutoutLabel: UILabel {
    override func drawText(in rect: CGRect) {
        super.drawText(in: rect.inset(by: .zero))
        guard let context = UIGraphicsGetCurrentContext() else { return }
        context.saveGState()
        context.setBlendMode(.clear)
        UIColor.darkBackground.setFill()
        UIRectFill(rect)
        super.drawText(in: rect)
        context.restoreGState()
    }
}
