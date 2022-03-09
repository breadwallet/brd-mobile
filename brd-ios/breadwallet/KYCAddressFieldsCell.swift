// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCAddressFieldsCell: UITableViewCell, GenericSettable {
    typealias Model = ViewModel
    
    struct ViewModel: Hashable {
        let country: String?
        let zipCode: String?
        let address: String?
        let apartment: String?
        let state: String?
    }
    
    private lazy var countryField: SimpleTextField = {
        let countryField = SimpleTextField()
        countryField.translatesAutoresizingMaskIntoConstraints = false
        countryField.setup(as: .picker, title: "COUNTRY", customPlaceholder: "Country")
        countryField.textField.addTarget(self, action: #selector(showCountryPicker(_:)),
                                         for: .touchDown)
        
        return countryField
    }()
    
    private lazy var zipCodeField: SimpleTextField = {
        let zipCodeField = SimpleTextField()
        zipCodeField.translatesAutoresizingMaskIntoConstraints = false
        zipCodeField.setup(as: .text, title: "ZIP CODE", customPlaceholder: "000 000")
        
        return zipCodeField
    }()
    
    private lazy var addressField: SimpleTextField = {
        let addressField = SimpleTextField()
        addressField.translatesAutoresizingMaskIntoConstraints = false
        addressField.setup(as: .text, title: "ADDRESS", customPlaceholder: "Street Number and Name")
        
        return addressField
    }()
    
    private lazy var apartmentField: SimpleTextField = {
        let apartmentField = SimpleTextField()
        apartmentField.translatesAutoresizingMaskIntoConstraints = false
        apartmentField.setup(as: .text, title: "", customPlaceholder: "Unit/Apartment")
        
        return apartmentField
    }()
    
    private lazy var cityField: SimpleTextField = {
        let cityField = SimpleTextField()
        cityField.translatesAutoresizingMaskIntoConstraints = false
        cityField.setup(as: .text, title: "CITY", customPlaceholder: "City")
        
        return cityField
    }()
    
    private lazy var stateField: SimpleTextField = {
        let stateField = SimpleTextField()
        stateField.translatesAutoresizingMaskIntoConstraints = false
        stateField.setup(as: .picker, title: "STATE", customPlaceholder: "State")
        stateField.textField.addTarget(self, action: #selector(showStatesPicker(_:)),
                                       for: .touchDown)
        
        return stateField
    }()
    
    private lazy var nextButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .enabled, title: "NEXT")
        
        return nextButton
    }()
    
    private lazy var privacyPolicyTextView: UITextView = {
        let privacyPolicyTextView = UITextView()
        privacyPolicyTextView.translatesAutoresizingMaskIntoConstraints = false
        privacyPolicyTextView.textAlignment = .center
        privacyPolicyTextView.textColor = .kycGray2
        privacyPolicyTextView.font = UIFont(name: "AvenirNext-Regular", size: 14)
        privacyPolicyTextView.isEditable = false
        privacyPolicyTextView.tintColor = .kycGray2
        
        return privacyPolicyTextView
    }()
    
    var didTapCountryPicker: (() -> Void)?
    var didChangeZipCodeField: ((String?) -> Void)?
    var didChangeAddressField: ((String?) -> Void)?
    var didChangeApartmentField: ((String?) -> Void)?
    var didChangeCityField: ((String?) -> Void)?
    var didTapStatePicker: (() -> Void)?
    var didTapNextButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        let defaultDistance: CGFloat = 12
        
        addSubview(countryField)
        countryField.topAnchor.constraint(equalTo: topAnchor).isActive = true
        countryField.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        countryField.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        countryField.heightAnchor.constraint(equalToConstant: 68).isActive = true
        
        addSubview(zipCodeField)
        zipCodeField.topAnchor.constraint(equalTo: countryField.bottomAnchor, constant: defaultDistance).isActive = true
        zipCodeField.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        zipCodeField.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        zipCodeField.heightAnchor.constraint(equalTo: countryField.heightAnchor).isActive = true
        
        addSubview(addressField)
        addressField.topAnchor.constraint(equalTo: zipCodeField.bottomAnchor, constant: defaultDistance).isActive = true
        addressField.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        addressField.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        addressField.heightAnchor.constraint(equalTo: countryField.heightAnchor).isActive = true
        
        addSubview(apartmentField)
        apartmentField.topAnchor.constraint(equalTo: addressField.bottomAnchor, constant: defaultDistance / 3).isActive = true
        apartmentField.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        apartmentField.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        apartmentField.heightAnchor.constraint(equalTo: countryField.heightAnchor).isActive = true
        
        addSubview(cityField)
        cityField.topAnchor.constraint(equalTo: apartmentField.bottomAnchor, constant: defaultDistance).isActive = true
        cityField.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        cityField.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        cityField.heightAnchor.constraint(equalTo: countryField.heightAnchor).isActive = true
        
        addSubview(stateField)
        stateField.topAnchor.constraint(equalTo: cityField.bottomAnchor, constant: defaultDistance).isActive = true
        stateField.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        stateField.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        stateField.heightAnchor.constraint(equalTo: countryField.heightAnchor).isActive = true
        
        addSubview(nextButton)
        nextButton.topAnchor.constraint(equalTo: stateField.bottomAnchor, constant: defaultDistance * 2).isActive = true
        nextButton.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        nextButton.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        nextButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        addSubview(privacyPolicyTextView)
        setupPrivacyPolicyText()
        privacyPolicyTextView.topAnchor.constraint(equalTo: nextButton.bottomAnchor, constant: defaultDistance).isActive = true
        privacyPolicyTextView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        privacyPolicyTextView.leadingAnchor.constraint(equalTo: countryField.leadingAnchor).isActive = true
        privacyPolicyTextView.trailingAnchor.constraint(equalTo: countryField.trailingAnchor).isActive = true
        privacyPolicyTextView.heightAnchor.constraint(equalTo: nextButton.heightAnchor).isActive = true
        
        zipCodeField.didChangeText = { [weak self] text in
            self?.didChangeZipCodeField?(text)
        }
        
        addressField.didChangeText = { [weak self] text in
            self?.didChangeAddressField?(text)
        }
        
        apartmentField.didChangeText = { [weak self] text in
            self?.didChangeApartmentField?(text)
        }
        
        cityField.didChangeText = { [weak self] text in
            self?.didChangeCityField?(text)
        }
        
        nextButton.didTap = { [weak self] in
            self?.didTapNextButton?()
        }
    }

    @objc private func showCountryPicker(_ textField: SimpleTextField) {
        didTapCountryPicker?()
    }
    
    @objc private func showStatesPicker(_ textField: SimpleTextField) {
        didTapStatePicker?()
    }
    
    private func setupPrivacyPolicyText() {
        let string = "Fabriik terms of use and privacy policy."
        let fullRange = (string as NSString).range(of: string)
        let termsRange = (string as NSString).range(of: "terms of use")
        let privacyRange = (string as NSString).range(of: "privacy policy")
        
        let style = NSMutableParagraphStyle()
        style.alignment = NSTextAlignment.center
        
        var attributedString = NSMutableAttributedString(string: string, attributes: [NSAttributedString.Key.paragraphStyle: style])
        
        attributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.kycGray2, range: fullRange)
        
        let url = "https://fabriik.com/terms-and-conditions/"
        createLink(attributedString: &attributedString, urlString: url, range: termsRange)
        createLink(attributedString: &attributedString, urlString: url, range: privacyRange)
        
        privacyPolicyTextView.attributedText = attributedString
    }
    
    private func createLink(attributedString: inout NSMutableAttributedString, urlString: String, range: NSRange) {
        guard let url = NSURL(string: urlString) else { return }
        
        attributedString.addAttribute(.link, value: url, range: range)
        attributedString.addAttribute(.underlineStyle, value: NSNumber(value: 1), range: range)
        attributedString.addAttribute(.underlineColor, value: UIColor.kycGray2, range: range)
    }
    
    func setup(with model: Model) {
        if let country = model.country {
            countryField.textField.text = country
        }
        
        if let zipCode = model.zipCode {
            zipCodeField.textField.text = zipCode
        }
        
        if let address = model.address {
            addressField.textField.text = address
        }
        
        if let apartment = model.apartment {
            apartmentField.textField.text = apartment
        }
        
        if let state = model.state {
            stateField.textField.text = state
        }
        
//        countryField.textField.text = model.country
//        areaCodeField.textField.text = model.areaCode
    }
}
