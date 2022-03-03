// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class RoundedView: UIView {
    private let defaultRadius: CGFloat = 8
    
    var cornerRadius: CGFloat {
        get {
            return layer.cornerRadius
        }
        set {
            layer.cornerRadius = newValue
            setupLayer()
        }
    }
    
    func setupDefaultRoundable() {
        cornerRadius = defaultRadius
    }
    
    func setupLayer() {
        clipsToBounds = true
        
        if #available(iOS 13.0, *) {
            layer.cornerCurve = .continuous
        }
    }
}
