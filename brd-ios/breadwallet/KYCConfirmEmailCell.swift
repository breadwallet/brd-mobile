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
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        label.textColor = .almostBlack
        label.font = UIFont(name: "AvenirNext-Bold", size: 20)
        label.text = "CONFIRM EMAIL"
        return label
    }()
    
    private lazy var descriptionLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        label.textColor = .kycGray1
        label.numberOfLines = 0
        label.font = UIFont(name: "AvenirNext-Regular", size: 19)
        label.text = "We’ve sent a confirmation code to your email. Click on it on this device to confirm your email address. \n\nYou can also copy and paste the code here:"
        return label
    }()
    
    private lazy var confirmationCodeField: SimpleTextField = {
        let textField = SimpleTextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.setup(as: .text, title: "CONFIRMATION CODE", customPlaceholder: "Confirmation code")
        return textField
    }()
    
    private lazy var confirmButton: KYCButton = {
        let button = KYCButton()
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setup(as: .disabled, title: "CONFIRM")
        return button
    }()
    
    var didChangeConfirmationCodeField: ((String?) -> Void)?
    var didTapConfirmButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(titleLabel)
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 36).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        
        addSubview(descriptionLabel)
        descriptionLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 22).isActive = true
        descriptionLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        descriptionLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        
        addSubview(confirmationCodeField)
        confirmationCodeField.topAnchor.constraint(equalTo: descriptionLabel.bottomAnchor, constant: 24).isActive = true
        confirmationCodeField.leadingAnchor.constraint(equalTo: descriptionLabel.leadingAnchor).isActive = true
        confirmationCodeField.trailingAnchor.constraint(equalTo: descriptionLabel.trailingAnchor).isActive = true
        confirmationCodeField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(confirmButton)
        confirmButton.topAnchor.constraint(equalTo: confirmationCodeField.bottomAnchor, constant: 12).isActive = true
        confirmButton.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        confirmButton.leadingAnchor.constraint(equalTo: confirmationCodeField.leadingAnchor).isActive = true
        confirmButton.trailingAnchor.constraint(equalTo: confirmationCodeField.trailingAnchor).isActive = true
        confirmButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        confirmationCodeField.didChangeText = { [weak self] text in
            self?.didChangeConfirmationCodeField?(text)
        }
    }
    
    @objc private func confirmAction() {
        didTapConfirmButton?()
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
