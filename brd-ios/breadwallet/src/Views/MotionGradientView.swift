//
//  MotionGradientView.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2018-06-14.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import CoreMotion

class MotionGradientView: UIView {

    private let motionLayer = MotionGradientLayer()

    init() {
        super.init(frame: .zero)
        motionLayer.frame = .zero
        layer.insertSublayer(motionLayer, at: 0)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        motionLayer.frame = bounds
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class MotionGradientLayer: CAGradientLayer {

    private var gradientColors = [UIColor.newGradientEnd.cgColor, UIColor.newGradientStart.cgColor]
    private var motion: CMMotionManager?
    private let queue = OperationQueue()
    
    override init() {
        super.init()
        setup()
    }
    
    override init(layer: Any) {
        super.init(layer: layer)
        setup()
    }

    private func setup() {
        startPoint = CGPoint(x: 1.0, y: 0.0) //top right
        endPoint = CGPoint(x: 0.0, y: 1.0) //bottom left
        drawsAsynchronously = true
        colors = gradientColors
        startMotion()
    }
    
    private func startMotion() {
        let motion = CMMotionManager()
        guard motion.isAccelerometerAvailable else { return }
        speed = 0
        add(CABasicAnimation.motionAnimation(colors: gradientColors.reversed()), forKey: "MotionShift")

        motion.startAccelerometerUpdates(to: queue, withHandler: { [weak self] (data, _) in
            guard let x = data?.acceleration.x, !x.isNaN else {
                return
            }
            DispatchQueue.main.async { [weak self] in
                self?.timeOffset = max(min(x + 0.5, 1), 0)
            }
        })
        self.motion = motion
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

fileprivate extension CABasicAnimation {
    class func motionAnimation(colors: [CGColor]) -> CABasicAnimation {
        let animation = CABasicAnimation(keyPath: "colors")
        animation.toValue = colors
        animation.fillMode = CAMediaTimingFillMode.forwards
        animation.isRemovedOnCompletion = false
        animation.duration = 1.0
        return animation
    }
}
