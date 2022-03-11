// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCFooterView: UIView {
    private lazy var logoImageView: UIImageView = {
        let logoImageView = UIImageView()
        logoImageView.translatesAutoresizingMaskIntoConstraints = false
        logoImageView.image = UIImage(named: "Powered By Fabriik Logo")
        logoImageView.contentMode = .scaleAspectFit
        logoImageView.backgroundColor = .clear
        
        return logoImageView
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
        backgroundColor = .clear
        
        addSubview(logoImageView)
        logoImageView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        logoImageView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        logoImageView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        logoImageView.heightAnchor.constraint(equalToConstant: 34).isActive = true
    }
}
