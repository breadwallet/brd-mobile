//
//  IndicatorButton.swift
//  breadwallet
//
//  Created by blockexplorer on 2021-09-01.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class IndicatorButton: UIButton {

    lazy var indicator: UIView = {
        let indicator = UIView()
        indicator.backgroundColor = Theme.accent
        indicator.clipsToBounds = true
        indicator.isHidden = true
        return indicator
    }()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if indicator.superview == nil {
            addSubview(indicator)
        }
        
        indicator.frame = CGRect(
            x: (titleLabel?.frame.minX ?? bounds.minX) - Constant.indicatorWidth * 2,
            y: (titleLabel?.frame.midY ?? bounds.maxY) - Constant.indicatorWidth / 4,
            width: Constant.indicatorWidth,
            height: Constant.indicatorWidth
        )
        
        indicator.layer.cornerRadius = Constant.indicatorWidth / 2
    }
}

// MARK: - Constant

private extension IndicatorButton {

    enum Constant {
        static let indicatorWidth: CGFloat = 6
    }
}
