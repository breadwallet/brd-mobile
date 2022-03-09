// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCButton: RoundedView {
    
    enum ButtonStyle {
        case enabled
        case disabled
        
        var borderColor: UIColor? {
            switch self {
            case .enabled:
                return .clear
                
            case .disabled:
                return .kycGray3
                
            }
        }
        
        var backgroundColor: UIColor {
            switch self {
            case .enabled:
                return .vibrantYellow
                
            case .disabled:
                return .kycCompletelyWhite
                
            }
        }
        
        var titleColor: UIColor {
            switch self {
            case .enabled:
                return .almostBlack
                
            case .disabled:
                return .kycGray3
                
            }
        }
    }
    
    private let button = BaseButton(type: .system)
    private var buttonStyle: ButtonStyle = .enabled
    
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
        button.layer.masksToBounds = true
        button.layer.cornerRadius = 13
        button.layer.borderColor = buttonStyle.borderColor?.cgColor
        button.layer.borderWidth = 2
        
        button.backgroundColor = buttonStyle.backgroundColor
        button.setTitleColor(buttonStyle.titleColor, for: .normal)
        button.isEnabled = buttonStyle != .disabled
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
