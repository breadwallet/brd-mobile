//
//  PreferredSizePresentationController.swift
//  breadwallet
//
//  Created by blockexplorer on 2021-08-31.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

/// ModalDismissProtocol implement to support handeling of dismissal via tap on
/// backgound chrome. Normally should be implemend by `presentedViewController`.
@objc protocol ModalDismissProtocol: AnyObject {
    weak var modalDismissDelegate: ModalDismissDelegate? { get set }
}

/// `ModalDismissDelegate` called by `ModalDismissProtocol`. Should be
/// implemented by `presentingViewController`.
@objc protocol ModalDismissDelegate: AnyObject {
    func viewControllerDismissActionPressed(_ viewController: UIViewController?)
}

/// PreferredSizePresentationController sizes `presentedViewController` by its
/// `preferredContentSize` and centers it in `presentingViewController`. If
/// `preferredContentSize` is not available or zero, size is computer using
/// `systemLayoutSizeFitting`. Stuble motion effect are added to
/// `presentedViewController`
class PreferredSizePresentationController: UIPresentationController {
    
    private weak var bgView: UIView?
    
    override var frameOfPresentedViewInContainerView: CGRect {
        get {
            var size = CGSize.zero
            let cb = containerView?.bounds ?? UIScreen.main.bounds
            // If preffered size is explicitly set use it
            if presentedViewController.preferredContentSize != .zero {
                size = presentedViewController.preferredContentSize
            // Compute size from presented view
            } else {
                var width = min(cb.width, cb.height)
                width = max(width * Const.widthScaler, Const.minWidth)
                let ts = CGSize(width: width - 2 * Const.margin, height: 0)
                let pView = presentedViewController.view
                pView?.layoutIfNeeded()
                let cSize = pView?.systemLayoutSizeFitting(ts,
                            withHorizontalFittingPriority: .required,
                            verticalFittingPriority: .fittingSizeLevel)
                size = cSize ?? size
            }
            let origin = CGPoint(x: cb.midX - size.width * 0.5,
                                 y: cb.midY - size.height * 0.5)
            let frame = CGRect(origin: origin, size: size)
            return frame
        }
    }
    
    override func presentationTransitionWillBegin() {
        super.presentationTransitionWillBegin()
        // Animate background
        // UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        let bgView = newBGView()
        containerView?.addSubview(bgView)
        self.bgView = bgView
        presentedView?.layer.cornerRadius = Const.cornerRadius
        UIView.animate(withDuration: Const.animDuration) {
            self.bgView?.alpha = 1
        }
    }
    
    override func presentationTransitionDidEnd(_ completed: Bool) {
        guard completed == true else { return }
        // Add motion effects
        let offset = 12
        [motionEffect(Const.vmPaht, type: .tiltAlongVerticalAxis),
        motionEffect(Const.hmPaht, type: .tiltAlongHorizontalAxis)]
            .forEach {
                $0.minimumRelativeValue = -offset
                $0.maximumRelativeValue = offset
                presentedView?.addMotionEffect($0)
            }
    }
    
    override func dismissalTransitionWillBegin() {
        super.dismissalTransitionWillBegin()
        UIView.animate(withDuration: Const.animDuration) {
            self.bgView?.alpha = 0
        }
    }
    
    override func dismissalTransitionDidEnd(_ completed: Bool) {
        super.dismissalTransitionDidEnd(completed)
        if completed {
            bgView?.removeFromSuperview()
        } else {
            UIView.animate(withDuration: Const.animDuration) {
                self.bgView?.alpha = 1
            }
        }
    }
    
    override func containerViewWillLayoutSubviews() {
        super.containerViewWillLayoutSubviews()
        bgView?.frame = containerView?.bounds ?? .zero
        presentedViewController.view.frame = frameOfPresentedViewInContainerView
    }
    
    override func adaptivePresentationStyle(for traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        return .custom
    }
    
    @objc func tapAction(_ recognizer: UITapGestureRecognizer) {
        if let vc = presentingViewController as? ModalDismissProtocol {
            vc.modalDismissDelegate?.viewControllerDismissActionPressed(presentedViewController)
        } else if let vc = presentedViewController as? ModalDismissDelegate {
            vc.viewControllerDismissActionPressed(presentedViewController)
        } else {
            presentingViewController.dismiss(animated: true, completion: nil)
        }
    }
    
    //  MARK: - Util funcs
    
    private func motionEffect(_ keyPath: String, type: UIInterpolatingMotionEffect.EffectType) -> UIInterpolatingMotionEffect {
        return UIInterpolatingMotionEffect(keyPath: keyPath, type: type)
    }
    
    private func newBGView() -> UIView {
        let frm = containerView?.bounds ?? presentingViewController.view.bounds
        let bgView = UIView(frame: frm)
        bgView.backgroundColor = UIColor.black.withAlphaComponent(Const.bgAlpha)
        bgView.alpha = 0
        let tap = UITapGestureRecognizer(target: self,
                                         action: #selector(tapAction(_:)))
        bgView.addGestureRecognizer(tap)
        return bgView
    }
    
    private struct Const {
        static let widthScaler: CGFloat = 0.91
        static let minWidth: CGFloat = 320
        static let margin: CGFloat = 8
        static let bgAlpha: CGFloat = 0.4
        static let hmPaht = "transform.translation.x"
        static let vmPaht = "transform.translation.y"
        static let cornerRadius: CGFloat = 16
        static let animDuration: TimeInterval = 0.2
    }
}
