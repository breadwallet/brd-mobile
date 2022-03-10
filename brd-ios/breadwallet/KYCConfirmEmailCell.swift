//
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCConfirmEmailCell: UITableViewCell, GenericSettable {
    typealias Model = ViewModel
    
    struct ViewModel: Hashable {
        let confirmationCode: String?
    }
    
    private lazy var titleLabel: UILabel = {
        let titleLabel = UILabel()
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.textAlignment = .center
        titleLabel.textColor = .almostBlack
        titleLabel.font = UIFont(name: "AvenirNext-Bold", size: 20)
        titleLabel.text = "CONFIRM EMAIL"
        
        return titleLabel
    }()
    
    private lazy var descriptionLabel: UILabel = {
        let descriptionLabel = UILabel()
        descriptionLabel.translatesAutoresizingMaskIntoConstraints = false
        descriptionLabel.textAlignment = .center
        descriptionLabel.textColor = .kycGray1
        descriptionLabel.numberOfLines = 0
        descriptionLabel.font = UIFont(name: "AvenirNext-Regular", size: 19)
        descriptionLabel.text = "We’ve sent a confirmation code to your email. Click on it on this device to confirm your email address. \n\nYou can also copy and paste the code here:"
        
        return descriptionLabel
    }()
    
    private lazy var confirmationCodeField: SimpleTextField = {
        let confirmationCodeField = SimpleTextField()
        confirmationCodeField.translatesAutoresizingMaskIntoConstraints = false
        confirmationCodeField.setup(as: .text, title: "CONFIRMATION CODE", customPlaceholder: "Confirmation code")
        
        return confirmationCodeField
    }()
    
    private lazy var confirmButton: KYCButton = {
        let confirmButton = KYCButton()
        confirmButton.translatesAutoresizingMaskIntoConstraints = false
        confirmButton.setup(as: .disabled, title: "CONFIRM")
        
        return confirmButton
    }()
    
    private lazy var resendButton: KYCButton = {
        let resendButton = KYCButton()
        resendButton.translatesAutoresizingMaskIntoConstraints = false
        resendButton.setup(as: .enabled, title: "RESEND CODE")
        
        return resendButton
    }()
    
    var didChangeConfirmationCodeField: ((String?) -> Void)?
    var didTapConfirmButton: (() -> Void)?
    var didTapResendButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(titleLabel)
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 36).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        
        let defaultDistance: CGFloat = 12
        
        addSubview(descriptionLabel)
        descriptionLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 22).isActive = true
        descriptionLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        descriptionLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        
        addSubview(confirmationCodeField)
        confirmationCodeField.topAnchor.constraint(equalTo: descriptionLabel.bottomAnchor, constant: 30).isActive = true
        confirmationCodeField.leadingAnchor.constraint(equalTo: descriptionLabel.leadingAnchor).isActive = true
        confirmationCodeField.trailingAnchor.constraint(equalTo: descriptionLabel.trailingAnchor).isActive = true
        confirmationCodeField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(confirmButton)
        confirmButton.topAnchor.constraint(equalTo: confirmationCodeField.bottomAnchor, constant: defaultDistance).isActive = true
        confirmButton.leadingAnchor.constraint(equalTo: confirmationCodeField.leadingAnchor).isActive = true
        confirmButton.trailingAnchor.constraint(equalTo: confirmationCodeField.trailingAnchor).isActive = true
        confirmButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        addSubview(resendButton)
        resendButton.topAnchor.constraint(equalTo: confirmButton.bottomAnchor, constant: defaultDistance * 3).isActive = true
        resendButton.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        resendButton.leadingAnchor.constraint(equalTo: confirmationCodeField.leadingAnchor).isActive = true
        resendButton.trailingAnchor.constraint(equalTo: confirmationCodeField.trailingAnchor).isActive = true
        resendButton.heightAnchor.constraint(equalTo: confirmButton.heightAnchor).isActive = true
        
        confirmationCodeField.didChangeText = { [weak self] text in
            self?.didChangeConfirmationCodeField?(text)
        }
        
        confirmButton.didTap = { [weak self] in
            self?.didTapConfirmButton?()
        }
        
        resendButton.didTap = { [weak self] in
            self?.didTapResendButton?()
        }
    }
    
    func setup(with model: ViewModel) {
        confirmationCodeField.textField.text = model.confirmationCode
    }
    
    func changeButtonStyle(with style: KYCButton.ButtonStyle) {
        confirmButton.changeStyle(with: style)
    }
    
    func changeFieldStyle(isViable: Bool) {
        confirmationCodeField.setCheckMark(isVisible: isViable)
    }
}
