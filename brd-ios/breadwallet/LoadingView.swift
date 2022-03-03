// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class LoadingView: UIView {
    private static var isShowing = false
    private static var topView: UIView? {
        return UIApplication.shared.windows.first(where: { $0.isKeyWindow })
    }
    
    // MARK: UI elements
    
    private lazy var roundedView: SpinnerView = {
        let view = SpinnerView()
        
        return view
    }()
    
    private lazy var blurView: UIVisualEffectView = {
        let blurEffect = UIBlurEffect(style: .dark)
        let view = UIVisualEffectView(effect: blurEffect)
        backgroundColor = UIColor.black.withAlphaComponent(0.2)
        
        return view
    }()
    
    // MARK: initialisation
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    // MARK: UI setup
    private func setupUI() {
        guard let topView = Self.topView else { return }
        
        addSubview(blurView)
        blurView.translatesAutoresizingMaskIntoConstraints = false
        blurView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        blurView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        blurView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        blurView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        
        addSubview(roundedView)
        roundedView.translatesAutoresizingMaskIntoConstraints = false
        roundedView.heightAnchor.constraint(equalToConstant: 56).isActive = true
        roundedView.widthAnchor.constraint(equalToConstant: 56).isActive = true
        roundedView.constrainToCenter()
        
        topView.addSubview(self)
        
        translatesAutoresizingMaskIntoConstraints = false
        topAnchor.constraint(equalTo: topAnchor).isActive = true
        bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
    }
    
    // MARK: show and hide trigger methods
    static func show(animated: Bool = true) {
        if isShowing {
            hide(animated: false)
        }
        
        guard !isShowing, let topView = topView else {
            return
        }
        
        isShowing = true
        
        UIApplication.shared.isIdleTimerDisabled = true
        
        let loadingView = LoadingView()
        topView.addSubview(loadingView)
        loadingView.translatesAutoresizingMaskIntoConstraints = false
        loadingView.topAnchor.constraint(equalTo: topView.topAnchor).isActive = true
        loadingView.bottomAnchor.constraint(equalTo: topView.bottomAnchor).isActive = true
        loadingView.leadingAnchor.constraint(equalTo: topView.leadingAnchor).isActive = true
        loadingView.trailingAnchor.constraint(equalTo: topView.trailingAnchor).isActive = true
        
        if !animated {
            loadingView.alpha = 1
        } else {
            loadingView.alpha = 0
            loadingView.transform = CGAffineTransform.identity.scaledBy(x: 1.8, y: 1.8)
            animate(withDuration: 0.15, delay: 0, options: .curveEaseOut) {
                loadingView.alpha = 1
                loadingView.transform = .identity
            }
        }
    }
    
    static func hide(animated: Bool = true) {
        guard isShowing, let topView = topView else { return }
        
        isShowing = false
        
        UIApplication.shared.isIdleTimerDisabled = false
        
        for view in topView.subviews where view is LoadingView {
            if !animated {
                view.alpha = 0
                view.removeFromSuperview()
            } else {
                animate(withDuration: 0.15, delay: 0, options: .curveEaseIn, animations: {
                    view.alpha = 0
                    view.transform = CGAffineTransform.identity.scaledBy(x: 1.8, y: 1.8)
                }, completion: { _ in
                    view.removeFromSuperview()
                })
            }
        }
    }
}
