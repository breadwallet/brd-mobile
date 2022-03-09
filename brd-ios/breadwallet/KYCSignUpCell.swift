// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCSignUpCell: UITableViewCell, GenericSettable {
    typealias Model = ViewModel
    
    struct ViewModel: Hashable {
        let firstName: String?
        let lastName: String?
        let email: String?
        let phonePrefix: String?
        let phoneNumber: String?
        let password: String?
        let tickBox: Bool?
    }
    
    private lazy var titleLabel: UILabel = {
        let titleLabel = UILabel()
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.textAlignment = .center
        titleLabel.textColor = .almostBlack
        titleLabel.font = UIFont(name: "AvenirNext-Bold", size: 20)
        titleLabel.text = "SIGN UP INFORMATION"
        
        return titleLabel
    }()
    
    private lazy var firstNameField: SimpleTextField = {
        let firstNameField = SimpleTextField()
        firstNameField.translatesAutoresizingMaskIntoConstraints = false
        firstNameField.setup(as: .text, title: "FIRST NAME", customPlaceholder: "First Name")
        
        return firstNameField
    }()
    
    private lazy var lastNameField: SimpleTextField = {
        let lastNameField = SimpleTextField()
        lastNameField.translatesAutoresizingMaskIntoConstraints = false
        lastNameField.setup(as: .text, title: "LAST NAME", customPlaceholder: "Last Name")
        
        return lastNameField
    }()
    
    private lazy var emailField: SimpleTextField = {
        let emailField = SimpleTextField()
        emailField.translatesAutoresizingMaskIntoConstraints = false
        emailField.setup(as: .email, title: "EMAIL", customPlaceholder: "Email Address")
        
        return emailField
    }()
    
    private lazy var phonePrefixField: SimpleTextField = {
        let phonePrefixField = SimpleTextField()
        phonePrefixField.translatesAutoresizingMaskIntoConstraints = false
        phonePrefixField.setup(as: .picker, title: "PHONE", customPlaceholder: "+1")
        phonePrefixField.roundSpecifiedCorners(maskedCorners: [.layerMinXMinYCorner, .layerMinXMaxYCorner])
        phonePrefixField.textField.addTarget(self, action: #selector(showPhonePrefixPicker(_:)),
                                             for: .touchDown)
        
        return phonePrefixField
    }()
    
    private lazy var phoneNumberField: SimpleTextField = {
        let phoneNumberField = SimpleTextField()
        phoneNumberField.translatesAutoresizingMaskIntoConstraints = false
        phoneNumberField.setup(as: .numbers, title: "", customPlaceholder: "(000)-000-0000")
        phoneNumberField.roundSpecifiedCorners(maskedCorners: [.layerMaxXMaxYCorner, .layerMaxXMinYCorner])
        
        return phoneNumberField
    }()
    
    private lazy var passwordField: SimpleTextField = {
        let passwordField = SimpleTextField()
        passwordField.translatesAutoresizingMaskIntoConstraints = false
        passwordField.setup(as: .password, title: "PASSWORD", customPlaceholder: "Minimum 8 characters")
        
        return passwordField
    }()
    
    private lazy var tickBoxView: KYCTickBoxView = {
        let tickBoxView = KYCTickBoxView()
        tickBoxView.translatesAutoresizingMaskIntoConstraints = false
        
        return tickBoxView
    }()
    
    private lazy var nextButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .disabled, title: "NEXT")
        
        return nextButton
    }()
    
    var didChangeFirstNameField: ((String?) -> Void)?
    var didChangeLastNameField: ((String?) -> Void)?
    var didChangeEmailField: ((String?) -> Void)?
    var didTapPhonePrefixField: (() -> Void)?
    var didChangePhoneNumberField: ((String?) -> Void)?
    var didChangePasswordField: ((String?) -> Void)?
    var didTickPrivacyPolicy: ((Bool) -> Void)?
    var didTapNextButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(titleLabel)
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 36).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        
        let defaultDistance: CGFloat = 12
        
        addSubview(firstNameField)
        firstNameField.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 30).isActive = true
        firstNameField.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        firstNameField.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        firstNameField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(lastNameField)
        lastNameField.topAnchor.constraint(equalTo: firstNameField.bottomAnchor, constant: defaultDistance).isActive = true
        lastNameField.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        lastNameField.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        lastNameField.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        
        addSubview(emailField)
        emailField.topAnchor.constraint(equalTo: lastNameField.bottomAnchor, constant: defaultDistance).isActive = true
        emailField.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        emailField.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        emailField.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        
        addSubview(phonePrefixField)
        phonePrefixField.topAnchor.constraint(equalTo: emailField.bottomAnchor, constant: defaultDistance).isActive = true
        phonePrefixField.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        phonePrefixField.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        phonePrefixField.widthAnchor.constraint(equalToConstant: 94).isActive = true
        
        addSubview(phoneNumberField)
        phoneNumberField.topAnchor.constraint(equalTo: emailField.bottomAnchor, constant: defaultDistance).isActive = true
        phoneNumberField.leadingAnchor.constraint(equalTo: phonePrefixField.trailingAnchor, constant: -1).isActive = true
        phoneNumberField.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        phoneNumberField.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        
        addSubview(passwordField)
        passwordField.topAnchor.constraint(equalTo: phonePrefixField.bottomAnchor, constant: defaultDistance).isActive = true
        passwordField.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        passwordField.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        passwordField.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        
        addSubview(tickBoxView)
        tickBoxView.topAnchor.constraint(equalTo: passwordField.bottomAnchor, constant: defaultDistance * 2).isActive = true
        tickBoxView.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        tickBoxView.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        tickBoxView.heightAnchor.constraint(equalTo: firstNameField.heightAnchor).isActive = true
        
        addSubview(nextButton)
        nextButton.topAnchor.constraint(equalTo: tickBoxView.bottomAnchor, constant: defaultDistance).isActive = true
        nextButton.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        nextButton.leadingAnchor.constraint(equalTo: firstNameField.leadingAnchor).isActive = true
        nextButton.trailingAnchor.constraint(equalTo: firstNameField.trailingAnchor).isActive = true
        nextButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        firstNameField.didChangeText = { [weak self] text in
            self?.didChangeFirstNameField?(text)
        }
        
        lastNameField.didChangeText = { [weak self] text in
            self?.didChangeLastNameField?(text)
        }
        
        emailField.didChangeText = { [weak self] text in
            self?.didChangeEmailField?(text)
        }
        
        phoneNumberField.didChangeText = { [weak self] text in
            self?.didChangePhoneNumberField?(text)
        }
        
        passwordField.didChangeText = { [weak self] text in
            self?.didChangePasswordField?(text)
        }
        
        tickBoxView.didTick = { [weak self] tickStatus in
            self?.didTickPrivacyPolicy?(tickStatus)
        }
        
        nextButton.didTap = { [weak self] in
            self?.didTapNextButton?()
        }
    }
    
    @objc private func showPhonePrefixPicker(_ textField: SimpleTextField) {
        didTapPhonePrefixField?()
    }
    
    func setup(with model: Model) {
        if let firstName = model.firstName {
            firstNameField.textField.text = firstName
        }
        
        if let lastName = model.lastName {
            lastNameField.textField.text = lastName
        }
        
        if let email = model.email {
            emailField.textField.text = email
        }
        
        if let phonePrefix = model.phonePrefix {
            phonePrefixField.textField.text = phonePrefix
        }
        
        if let phoneNumber = model.phoneNumber {
            phoneNumberField.textField.text = phoneNumber
        }
        
        if let password = model.password {
            passwordField.textField.text = password
        }
        
        if let tickBox = model.tickBox {
            tickBoxView.toggle(with: tickBox)
        }
    }
    
    func changeButtonStyle(with style: KYCButton.ButtonStyle) {
        nextButton.changeStyle(with: style)
    }
}
