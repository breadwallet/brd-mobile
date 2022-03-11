// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCTickBoxView: UIView {
    private lazy var tickButton: UIButton = {
        let tickButton = UIButton()
        tickButton.translatesAutoresizingMaskIntoConstraints = false
        tickButton.setImage(UIImage(named: "KYC Unticked"), for: .normal)
        tickButton.setImage(UIImage(named: "KYC Ticked"), for: .selected)
        tickButton.addTarget(self, action: #selector(tickBoxTapped), for: .touchUpInside)
        
        return tickButton
    }()
    
    private lazy var noticeLabel: UITextView = {
        let noticeLabel = UITextView()
        noticeLabel.translatesAutoresizingMaskIntoConstraints = false
        noticeLabel.textAlignment = .left
        noticeLabel.font = UIFont(name: "AvenirNext-Regular", size: 14)
        noticeLabel.isEditable = false
        noticeLabel.textColor = .kycGray1
        noticeLabel.tintColor = .kycGray1
        
        return noticeLabel
    }()
    
    var didTick: ((Bool) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    private func setupUI() {
        backgroundColor = .clear
        
        addSubview(tickButton)
        tickButton.topAnchor.constraint(equalTo: topAnchor).isActive = true
        tickButton.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        tickButton.heightAnchor.constraint(equalToConstant: 34).isActive = true
        tickButton.widthAnchor.constraint(equalToConstant: 34).isActive = true
        
        addSubview(noticeLabel)
        noticeLabel.topAnchor.constraint(equalTo: tickButton.topAnchor).isActive = true
        noticeLabel.leadingAnchor.constraint(equalTo: tickButton.trailingAnchor, constant: 8).isActive = true
        noticeLabel.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        noticeLabel.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        
        setupPrivacyPolicyText()
    }
    
    @objc private func tickBoxTapped() {
        toggle()
        
        didTick?(tickButton.isSelected)
    }
    
    func toggle(with value: Bool? = nil) {
        if let value = value {
            tickButton.isSelected = value
        } else {
            tickButton.isSelected = !tickButton.isSelected
        }
    }
    
    private func setupPrivacyPolicyText() {
        let string = "I certify that I am 18 years of age or older, and I agree to the User Agreement and Privacy Policy"
        let fullRange = (string as NSString).range(of: string)
        let termsRange = (string as NSString).range(of: "User Agreement")
        let privacyRange = (string as NSString).range(of: "Privacy Policy")
        
        let style = NSMutableParagraphStyle()
        style.alignment = NSTextAlignment.left
        
        var attributedString = NSMutableAttributedString(string: string, attributes: [NSAttributedString.Key.paragraphStyle: style])
        
        attributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.kycGray1, range: fullRange)
        
        let url = "https://fabriik.com/terms-and-conditions/"
        createLink(attributedString: &attributedString, urlString: url, range: termsRange)
        createLink(attributedString: &attributedString, urlString: url, range: privacyRange)
        
        noticeLabel.attributedText = attributedString
    }
    
    private func createLink(attributedString: inout NSMutableAttributedString, urlString: String, range: NSRange) {
        guard let url = NSURL(string: urlString) else { return }
        
        attributedString.addAttribute(.link, value: url, range: range)
        attributedString.addAttribute(.underlineStyle, value: NSNumber(value: 1), range: range)
        attributedString.addAttribute(.underlineColor, value: UIColor.kycGray1, range: range)
    }
}
