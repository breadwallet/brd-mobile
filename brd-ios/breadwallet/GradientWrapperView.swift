//
// Created by blockexplorer on 17/05/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import UIKit

class GradientWrapperView: UIView {

    var startPoint: CGPoint = CGPoint(x: 0, y: 0.5)
    var endPoint: CGPoint = CGPoint(x: 1, y: 0.5)
    
    var type: CAGradientLayerType = .axial {
        didSet { gradientLayer()?.type = type }
    }
    
    var colors: [UIColor?] = [UIColor.clear] {
        didSet { updateColors() }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    override class var layerClass: AnyClass {
        return CAGradientLayer.self
    }
}

private extension GradientWrapperView {

    func setupUI() {
        gradientLayer()?.type = type
        gradientLayer()?.startPoint = startPoint
        gradientLayer()?.endPoint = endPoint
        updateColors()
    }

    func updateColors() {
        var colors: [UIColor?] = self.colors
        if colors.count == 1 {
            colors = [colors[0], colors[0]]
        }
        gradientLayer()?.colors = colors.map { ($0 ?? UIColor.clear).cgColor }
    }

    func gradientLayer() -> CAGradientLayer? {
        return layer as? CAGradientLayer
    }
}
