// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCPersonalInfoCell: UITableViewCell, GenericSettable {
    typealias Model = ViewModel
    
    struct ViewModel: Hashable {
        let date: String?
        let taxIdNumber: String?
    }
    
    private lazy var dateOfBirthField: SimpleTextField = {
        let dateOfBirthField = SimpleTextField()
        dateOfBirthField.translatesAutoresizingMaskIntoConstraints = false
        dateOfBirthField.setup(as: .picker, textFieldType: .none, title: "DATE OF BIRTH", customPlaceholder: "DD/MM/YYYY")
        dateOfBirthField.textField.addTarget(self, action: #selector(showDateOfBirthPicker(_:)),
                                             for: .touchDown)
        
        return dateOfBirthField
    }()
    
    private lazy var taxIdNumberField: SimpleTextField = {
        let taxIdNumberField = SimpleTextField()
        taxIdNumberField.translatesAutoresizingMaskIntoConstraints = false
        taxIdNumberField.setup(as: .numbers, textFieldType: .none, title: "TAX ID NUMBER", customPlaceholder: "000 000 000")
        
        return taxIdNumberField
    }()
    
    private lazy var nextButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .normal, title: "NEXT")
        
        return nextButton
    }()
    
    var didTapDateOfBirthField: (() -> Void)?
    var didChangeTaxIdNumberField: ((String?) -> Void)?
    var didTapNextButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        let defaultDistance: CGFloat = 12
        
        addSubview(dateOfBirthField)
        dateOfBirthField.topAnchor.constraint(equalTo: topAnchor).isActive = true
        dateOfBirthField.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        dateOfBirthField.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        dateOfBirthField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(taxIdNumberField)
        taxIdNumberField.topAnchor.constraint(equalTo: dateOfBirthField.bottomAnchor, constant: defaultDistance).isActive = true
        taxIdNumberField.leadingAnchor.constraint(equalTo: dateOfBirthField.leadingAnchor).isActive = true
        taxIdNumberField.trailingAnchor.constraint(equalTo: dateOfBirthField.trailingAnchor).isActive = true
        taxIdNumberField.heightAnchor.constraint(equalTo: dateOfBirthField.heightAnchor).isActive = true
        
        addSubview(nextButton)
        nextButton.topAnchor.constraint(equalTo: taxIdNumberField.bottomAnchor, constant: defaultDistance * 2).isActive = true
        nextButton.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        nextButton.leadingAnchor.constraint(equalTo: dateOfBirthField.leadingAnchor).isActive = true
        nextButton.trailingAnchor.constraint(equalTo: dateOfBirthField.trailingAnchor).isActive = true
        nextButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        taxIdNumberField.didChangeText = { [weak self] text in
            self?.didChangeTaxIdNumberField?(text)
        }
        
        nextButton.didTap = { [weak self] in
            self?.didTapNextButton?()
        }
    }
    
    @objc private func showDateOfBirthPicker(_ textField: SimpleTextField) {
        didTapDateOfBirthField?()
    }
    
    func setup(with model: Model) {
        if let date = model.date {
            dateOfBirthField.textField.text = date
        }
        
        if let taxIdNumber = model.taxIdNumber {
            taxIdNumberField.textField.text = taxIdNumber
        }
        
//        countryField.textField.text = model.country
//        areaCodeField.textField.text = model.areaCode
    }
}
