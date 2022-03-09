// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCSignInCell: UITableViewCell, GenericSettable {
    typealias Model = ViewModel
    
    struct ViewModel: Hashable {
        let email: String?
        let password: String?
    }
    
    private lazy var titleLabel: UILabel = {
        let titleLabel = UILabel()
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.textAlignment = .center
        titleLabel.textColor = .almostBlack
        titleLabel.font = UIFont(name: "AvenirNext-Bold", size: 20)
        titleLabel.text = "LOG IN"
        
        return titleLabel
    }()
    
    private lazy var emailField: SimpleTextField = {
        let emailField = SimpleTextField()
        emailField.translatesAutoresizingMaskIntoConstraints = false
        emailField.setup(as: .email, title: "EMAIL", customPlaceholder: "Email Address")
        
        return emailField
    }()
    
    private lazy var passwordField: SimpleTextField = {
        let passwordField = SimpleTextField()
        passwordField.translatesAutoresizingMaskIntoConstraints = false
        passwordField.setup(as: .password, title: "PASSWORD", customPlaceholder: "Minimum 8 characters")
        
        return passwordField
    }()
    
    private lazy var nextButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .disabled, title: "SUBMIT")
        
        return nextButton
    }()
    
    private lazy var accountNoticeLabel: UILabel = {
        let accountNoticeLabel = UILabel()
        accountNoticeLabel.translatesAutoresizingMaskIntoConstraints = false
        accountNoticeLabel.textAlignment = .left
        accountNoticeLabel.font = UIFont(name: "AvenirNext-Regular", size: 16)
        accountNoticeLabel.text = "Don’t have an account?"
        accountNoticeLabel.textColor = .kycGray2
        
        return accountNoticeLabel
    }()
    
    private lazy var signUpButton: UIButton = {
        let signUpButton = UIButton()
        signUpButton.translatesAutoresizingMaskIntoConstraints = false
        signUpButton.addTarget(self, action: #selector(signUpAction), for: .touchUpInside)
        
        let attributes: [NSAttributedString.Key: Any] = [
            .font: UIFont(name: "AvenirNext-Regular", size: 16) ?? UIFont.systemFont(ofSize: 16),
            .foregroundColor: UIColor.vibrantYellow,
            .underlineStyle: NSUnderlineStyle.single.rawValue
        ]
        let attributeString = NSMutableAttributedString(
            string: "Sign Up",
            attributes: attributes
        )
        signUpButton.setAttributedTitle(attributeString, for: .normal)
        
        return signUpButton
    }()
    
    private lazy var signUpStack: UIStackView = {
        let signUpStack = UIStackView()
        signUpStack.translatesAutoresizingMaskIntoConstraints = false
        signUpStack.backgroundColor = .clear
        signUpStack.axis = .horizontal
        signUpStack.spacing = 4
        
        return signUpStack
    }()
    
    var didChangeEmailField: ((String?) -> Void)?
    var didChangePasswordField: ((String?) -> Void)?
    var didTapNextButton: (() -> Void)?
    var didTapSignUpButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(titleLabel)
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 36).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        
        let defaultDistance: CGFloat = 12
        
        addSubview(emailField)
        emailField.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 30).isActive = true
        emailField.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        emailField.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        emailField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(passwordField)
        passwordField.topAnchor.constraint(equalTo: emailField.bottomAnchor, constant: defaultDistance).isActive = true
        passwordField.leadingAnchor.constraint(equalTo: emailField.leadingAnchor).isActive = true
        passwordField.trailingAnchor.constraint(equalTo: emailField.trailingAnchor).isActive = true
        passwordField.heightAnchor.constraint(equalTo: emailField.heightAnchor).isActive = true
        
        addSubview(nextButton)
        nextButton.topAnchor.constraint(equalTo: passwordField.bottomAnchor, constant: defaultDistance * 2).isActive = true
        nextButton.leadingAnchor.constraint(equalTo: emailField.leadingAnchor).isActive = true
        nextButton.trailingAnchor.constraint(equalTo: emailField.trailingAnchor).isActive = true
        nextButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        addSubview(signUpStack)
        signUpStack.topAnchor.constraint(equalTo: nextButton.bottomAnchor, constant: defaultDistance * 2).isActive = true
        signUpStack.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        signUpStack.centerXAnchor.constraint(equalTo: emailField.centerXAnchor).isActive = true
        signUpStack.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        signUpStack.addArrangedSubview(accountNoticeLabel)
        signUpStack.addArrangedSubview(signUpButton)
        
        emailField.didChangeText = { [weak self] text in
            self?.didChangeEmailField?(text)
        }
        
        passwordField.didChangeText = { [weak self] text in
            self?.didChangePasswordField?(text)
        }
        
        nextButton.didTap = { [weak self] in
            self?.didTapNextButton?()
        }
    }
    
    @objc private func signUpAction() {
        didTapSignUpButton?()
    }
    
    func setup(with model: Model) {
        if let email = model.email {
            emailField.textField.text = email
        }
        
        if let password = model.password {
            passwordField.textField.text = password
        }
    }
    
    func changeButtonStyle(with style: KYCButton.ButtonStyle) {
        nextButton.changeStyle(with: style)
    }
    
    func changeFieldStyle(isViable: Bool, for fieldType: KYCSignIn.FieldType) {
        switch fieldType {
        case .email:
            emailField.setCheckMark(isVisible: isViable)
            
        case .password:
            passwordField.setCheckMark(isVisible: isViable)
            
        }
    }
}
