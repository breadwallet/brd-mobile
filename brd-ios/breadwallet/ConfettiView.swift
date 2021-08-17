//
//  ConfettyView.swift
//  breadwallet
//
//  Created by stringcode on 29/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class ConfettyView: UIView {

    enum Style {
        case `default`
        case doge
    }

    private lazy var colors = [
        UIColor.fromHex("FD4E1F"), UIColor.fromHex("5B6DEE"),
        UIColor.fromHex("2AB8E6"), UIColor.fromHex("F5A623"),
        UIColor.fromHex("BD10E0"), UIColor.fromHex("0AD78C")
    ]

    private lazy var images = [
        UIImage(named: "confetty00"), UIImage(named: "confetty01"),
        UIImage(named: "confetty02"), UIImage(named: "confetty03"),
        UIImage(named: "confetty04")
    ]

    private lazy var dogeImages = [
        UIImage(named: "confettyDoge00"), UIImage(named: "confettyDoge01"),
        UIImage(named: "confettyDoge02"), UIImage(named: "confettyDoge03"),
        UIImage(named: "confettyDoge00")
    ]

    private lazy var haptics = UINotificationFeedbackGenerator()
    private weak var emitter: CAEmitterLayer?

    var style: Style = .doge

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.initialSetup()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialSetup()
    }

    func animateConfetty() {
        // NOTE: First delay is work around iOS bug where emitter sometimes does
        // emittes partibles until after some internal machinery (after
        // `viewDidAppear` is called) is done
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { [weak self] in
            self?.addEmitterIfNeeded()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak self] in
                self?.startAnimating()
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
                    self?.stopAnimating()
                }
            }
        }
    }

    func startAnimating() {
        let anim = CABasicAnimation(keyPath: Constant.lifetimeKey)
        anim.fromValue = emitter?.presentation()?.lifetime ?? 0
        anim.toValue = 1
        anim.fillMode = .both
        anim.isRemovedOnCompletion = false
        anim.duration = Constant.animDuration
        emitter?.add(anim, forKey: Constant.lifetimeKey)
        playImpact()
    }

    func stopAnimating() {
        guard let emitterPresentation = emitter?.presentation() else {
            return
        }

        let anim = CAKeyframeAnimation(keyPath: Constant.lifetimeKey)
        anim.values = [emitterPresentation.lifetime, emitterPresentation.lifetime, 0.0]
        anim.keyTimes = [0.0, 0.5, 1.0]
        anim.fillMode = .both
        anim.isRemovedOnCompletion = false
        anim.duration = Constant.animDuration
        emitter?.add(anim, forKey: Constant.lifetimeKey)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        emitter?.bounds = CGRect(x: 0, y: 0, width: bounds.width, height: 1)
        emitter?.emitterSize = CGSize(width: bounds.width, height: 1)
        emitter?.position = CGPoint(x: bounds.width, y: 0)
    }

}

// MARK: - UISetup

private extension ConfettyView {

    func initialSetup() {
        backgroundColor = UIColor.clear
        isUserInteractionEnabled = false
        clipsToBounds = false
    }

    func addEmitterIfNeeded() {
        guard emitter == nil else {
            return
        }

        let emitter = CAEmitterLayer()
        emitter.emitterShape = .line
        emitter.renderMode = .oldestLast
        emitter.lifetime = 0

        var emitterCells = images.enumerated().map {
            ConfettyView.newEmitterCell(image: $0.1, color: colors[$0.0])
        }

        if style == .doge {
            emitterCells += dogeImages.enumerated().map {
                ConfettyView.newDogeEmitterCell(image: $0.1)
            }
        }

        emitter.emitterCells = emitterCells

        self.emitter = emitter
        layer.addSublayer(emitter)
        setNeedsLayout()
    }

    func playImpact() {
        haptics.notificationOccurred(.success)
    }

    class func newEmitterCell(image: UIImage? = nil, color: UIColor? = nil) -> CAEmitterCell {
        let cell = CAEmitterCell()
        cell.contents = image?.cgImage
        cell.birthRate = 30
        cell.lifetime = 25
        cell.scale = 0.35
        cell.scaleRange = 0.1
        cell.spin = 3
        cell.spinRange = 3
        cell.velocity = -300
        cell.velocityRange = -50.0
        cell.color = color?.cgColor
        return cell
    }

    class func newDogeEmitterCell(image: UIImage? = nil) -> CAEmitterCell {
        let cell = CAEmitterCell()
        cell.contents = image?.cgImage
        cell.birthRate = 2
        cell.lifetime = 25
        cell.scale = 0.35
        cell.scaleRange = 0.1
        cell.spin = 1
        cell.spinRange = 1
        cell.velocity = -300
        cell.velocityRange = -50.0
        return cell
    }
}

// MARK: - Constant

private extension ConfettyView {

    enum Constant {
        static let lifetimeKey = "lifetime"
        static let animDuration = 0.05
    }
}
