// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCButton: RoundedView {
    enum ButtonStyle {
        case normal
        case almostBlack
        
        var backgroundColor: UIColor {
            switch self {
            case .normal:
                return .vibrantYellow
            case .almostBlack:
                return .almostBlack
            }
        }
        
        var titleColor: UIColor {
            switch self {
            case .normal:
                return .almostBlack
            case .almostBlack:
                return .vibrantYellow
            }
        }
    }
    
    private let button = BaseButton(type: .system)
    private var buttonStyle: ButtonStyle = .normal
    
    var didTap: (() -> Void)?
    
    init() {
        super.init(frame: .zero)
        
        setupElements()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        setupElements()
    }
    
    func setup(as buttonStyle: ButtonStyle, title: String) {
        self.buttonStyle = buttonStyle
        
        button.setTitle(title, for: .normal)
        
        style()
    }
    
    func changeStyle(with buttonStyle: ButtonStyle) {
        self.buttonStyle = buttonStyle
        
        style()
    }
    
    private func style() {
        button.backgroundColor = buttonStyle.backgroundColor
        button.setTitleColor(buttonStyle.titleColor, for: .normal)
        button.layer.borderColor = UIColor.vibrantYellow.cgColor
        button.layer.borderWidth = 2
        button.layer.cornerRadius = 13
//        button.isEnabled = buttonStyle != .inactive
    }
    
    private func setupElements() {
        backgroundColor = .clear
        
        addSubview(button)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.topAnchor.constraint(equalTo: topAnchor).isActive = true
        button.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        button.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        button.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        
        button.titleLabel?.font = UIFont(name: "AvenirNext-Bold", size: 14)
        button.addTarget(self, action: #selector(buttonTapped), for: .touchUpInside)
    }
    
    @objc private func buttonTapped() {
        didTap?()
    }
}
