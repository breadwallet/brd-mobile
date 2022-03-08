// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class SimpleTextField: UIView, UITextFieldDelegate {
    enum FieldType {
        case text, numbers, picker
    }
    
    enum TextFieldType {
        case email, password, none
    }
    
    private var fieldType: FieldType = .text
    private var textFieldType: TextFieldType = .none
    
    private lazy var rightButton: UIButton = {
        let rightButton = UIButton()
        rightButton.translatesAutoresizingMaskIntoConstraints = false
        
        return rightButton
    }()
    
    private lazy var titleLabel: UILabel = {
        var titleLabel = UILabel()
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.textColor = .kycGray2
        titleLabel.font = UIFont(name: "AvenirNext-Medium", size: 12)
        titleLabel.textAlignment = .left
        titleLabel.numberOfLines = 1
        
        return titleLabel
    }()
    
    lazy var textField: PaddedTextField = {
        var textField = PaddedTextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.delegate = self
        textField.textColor = .kycGray1
        
        return textField
    }()
    
    var didChangeText: ((String?) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        textField.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        textField.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
    }
    
    func setup(as fieldType: FieldType, textFieldType: TextFieldType, title: String, customPlaceholder: String? = nil) {
        self.fieldType = fieldType
        self.textFieldType = textFieldType
        
        textField.attributedPlaceholder = NSAttributedString(string: customPlaceholder ?? "",
                                                             attributes: [.foregroundColor: UIColor.kycGray1,
                                                                          .font: UIFont(name: "AvenirNext-Medium", size: 16) ?? UIFont.systemFont(ofSize: 16)])
        textField.delegate = self
        
        titleLabel.text = title
        
        switch fieldType {
        case .text:
            textField.keyboardType = .default
            textField.autocapitalizationType = .sentences
            textField.autocorrectionType = .no
            
        case .numbers:
            textField.keyboardType = .numberPad
            
        case .picker:
            rightButton.setImage(UIImage(named: "KYC Dropdown Arrow"), for: .normal)
            rightButton.isUserInteractionEnabled = false
            textField.inputView = UIView()
            
        }
        
        setupElements()
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField) {
        switch fieldType {
        case .picker:
            endEditing(true)
            resignFirstResponder()
        default:
            break
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        guard textField.text != nil else { return }
        
        switch textFieldType {
        case .email, .password:
            textField.layer.borderColor = (isValid ? UIColor.green.cgColor  : UIColor.red.cgColor)
            rightButton.setImage(UIImage(named: "checkMark"), for: .normal)
            rightButton.isUserInteractionEnabled = false
            rightButton.isHidden = !isValid
            textField.inputView = UIView()
            rightButton.trailingAnchor.constraint(equalTo: textField.trailingAnchor, constant: -10).isActive = true
        case .none: break
        }
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        switch fieldType {
        case .numbers:
            let allowedCharacters = CharacterSet.decimalDigits
            let characterSet = CharacterSet(charactersIn: string)
            let isOnlyNumbersAllowed = allowedCharacters.isSuperset(of: characterSet)
            
            return isOnlyNumbersAllowed
            
        default:
            return true
            
        }
    }
    
    var isValid: Bool {
        guard let text = textField.text else { return false }
        var isValid = false
        
        switch textFieldType {
        case .email:
            isValid = text.isValidEmailAddress
        case .password:
            isValid = text.count >= 8
        case .none:
            isValid = true
        }
        return isValid
    }
    
    private func setupElements() {
        backgroundColor = .clear
        
        addSubview(titleLabel)
        titleLabel.topAnchor.constraint(equalTo: topAnchor).isActive = true
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        titleLabel.heightAnchor.constraint(equalToConstant: 16).isActive = true
        
        addSubview(textField)
        textField.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 4).isActive = true
        textField.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        textField.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        textField.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        textField.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        textField.layer.masksToBounds = true
        textField.layer.cornerRadius = 4
        textField.layer.borderColor = UIColor.kycGray1.cgColor
        textField.layer.borderWidth = 1
        textField.clipsToBounds = true
        
        textField.addSubview(rightButton)
        rightButton.topAnchor.constraint(equalTo: textField.topAnchor).isActive = true
        rightButton.bottomAnchor.constraint(equalTo: textField.bottomAnchor).isActive = true
        rightButton.trailingAnchor.constraint(equalTo: textField.trailingAnchor).isActive = true
        rightButton.heightAnchor.constraint(equalTo: textField.heightAnchor).isActive = true
    }
    
    @objc private func textFieldDidChange(_ textField: UITextField) {
        didChangeText?(textField.text)
    }
}
