//
//  AddressCell.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-12-16.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

class AddressCell: UIView {

    init(currency: Currency, nativeCurrencyCode: String) {
        self.currency = currency
        self.nativeCurrencyCode = nativeCurrencyCode
        super.init(frame: .zero)
        setupViews()
    }

    var address: String? {
        return contentLabel.text
    }

    var textDidChange: ((String?) -> Void)?
    var didBeginEditing: (() -> Void)?
    var didReceivePaymentRequest: ((PaymentRequest) -> Void)?
    var didReceiveResolvedAddress: ((AddressResult?, AddressType) -> Void)?
    
    func setContent(_ content: String?) {
        contentLabel.text = content
        textField.text = content
        textDidChange?(content)
    }

    var isEditable = false {
        didSet {
            gr.isEnabled = isEditable
        }
    }
    
    func hideActionButtons() {
        paste.isHidden = true
        paste.isEnabled = false
        scan.isHidden = true
        scan.isEnabled = false
    }
    
    func showActionButtons() {
        paste.isHidden = false
        paste.isEnabled = true
        scan.isHidden = false
        scan.isEnabled = true
    }

    let textField = UITextField()
    let paste = BRDButton(title: S.Send.pasteLabel, type: .tertiary)
    let scan = BRDButton(title: S.Send.scanLabel, type: .tertiary)
    fileprivate let contentLabel = UILabel(font: .customBody(size: 14.0), color: .darkText)
    private let label = UILabel(font: .customBody(size: 16.0))
    fileprivate let gr = UITapGestureRecognizer()
    fileprivate let tapView = UIView()
    private let border = UIView(color: .secondaryShadow)
    private let resolvedAddressLabel = ResolvedAddressLabel()
    private let activityIndicator = UIActivityIndicatorView(style: .gray)
    
    func showResolveableState(type: AddressType, address: String) {
        textField.resignFirstResponder()
        label.isHidden = true
        resolvedAddressLabel.isHidden = false
        activityIndicator.stopAnimating()
        activityIndicator.isHidden = true
        isEditable = true
        resolvedAddressLabel.type = type
        resolvedAddressLabel.address = address
    }
    
    func hideResolveableState() {
        label.isHidden = false
        resolvedAddressLabel.isHidden = true
        activityIndicator.stopAnimating()
        activityIndicator.isHidden = true
        isEditable = true
    }
    
    func showResolvingSpinner() {
        label.isHidden = true
        addSubview(activityIndicator)
        activityIndicator.constrain([
            activityIndicator.topAnchor.constraint(equalTo: topAnchor, constant: C.padding[1]),
            activityIndicator.constraint(.leading, toView: self, constant: C.padding[2]) ])
        activityIndicator.startAnimating()
    }
    
    fileprivate let currency: Currency
    fileprivate let nativeCurrencyCode: String

    private func setupViews() {
        addSubviews()
        addConstraints()
        setInitialData()
    }

    private func addSubviews() {
        addSubview(resolvedAddressLabel)
        addSubview(label)
        addSubview(contentLabel)
        addSubview(textField)
        addSubview(tapView)
        addSubview(border)
        addSubview(paste)
        addSubview(scan)
    }

    private func addConstraints() {
        label.constrain([
            label.constraint(.leading, toView: self, constant: C.padding[2]),
            label.topAnchor.constraint(equalTo: topAnchor, constant: C.padding[1])])
        resolvedAddressLabel.constrain([
            resolvedAddressLabel.topAnchor.constraint(equalTo: topAnchor, constant: C.padding[1]),
            resolvedAddressLabel.constraint(.leading, toView: self, constant: C.padding[2]) ])
        resolvedAddressLabel.isHidden = true
        
        contentLabel.constrain([
            contentLabel.constraint(.leading, toView: label),
            contentLabel.constraint(toBottom: label, constant: 0.0),
            contentLabel.trailingAnchor.constraint(equalTo: paste.leadingAnchor, constant: -C.padding[1]) ])
        textField.constrain([
            textField.constraint(.leading, toView: label),
            textField.constraint(toBottom: label, constant: 0.0),
            textField.trailingAnchor.constraint(equalTo: paste.leadingAnchor, constant: -C.padding[1]) ])
        tapView.constrain([
            tapView.leadingAnchor.constraint(equalTo: leadingAnchor),
            tapView.topAnchor.constraint(equalTo: topAnchor),
            tapView.bottomAnchor.constraint(equalTo: bottomAnchor),
            tapView.trailingAnchor.constraint(equalTo: paste.leadingAnchor) ])
        scan.constrain([
            scan.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -C.padding[2]),
            scan.centerYAnchor.constraint(equalTo: centerYAnchor),
            scan.heightAnchor.constraint(equalToConstant: 32.0)])
        paste.constrain([
            paste.centerYAnchor.constraint(equalTo: centerYAnchor),
            paste.trailingAnchor.constraint(equalTo: scan.leadingAnchor, constant: -C.padding[1]),
            paste.heightAnchor.constraint(equalToConstant: 33.0)
        ])
        border.constrain([
            border.leadingAnchor.constraint(equalTo: leadingAnchor),
            border.bottomAnchor.constraint(equalTo: bottomAnchor),
            border.trailingAnchor.constraint(equalTo: trailingAnchor),
            border.heightAnchor.constraint(equalToConstant: 1.0) ])
    }

    private func setInitialData() {
        label.text = S.Send.toLabel
        textField.font = contentLabel.font
        textField.textColor = contentLabel.textColor
        textField.isHidden = true
        textField.returnKeyType = .done
        textField.delegate = self
        textField.clearButtonMode = .whileEditing
        textField.autocorrectionType = .no
        textField.autocapitalizationType = .none
        textField.keyboardType = .emailAddress
        textField.addTarget(self, action: #selector(textFieldDidChange), for: .editingChanged)
        label.textColor = .grayTextTint
        contentLabel.lineBreakMode = .byTruncatingMiddle

        textField.editingChanged = strongify(self) { myself in
            myself.contentLabel.text = myself.textField.text
        }

        //GR to start editing label
        gr.addTarget(self, action: #selector(didTap))
        tapView.addGestureRecognizer(gr)
    }

    @objc private func didTap() {
        textField.becomeFirstResponder()
        contentLabel.isHidden = true
        textField.isHidden = false
        showActionButtons()
        resolvedAddressLabel.type = nil
        resolvedAddressLabel.address = nil
    }
    
    @objc private func textFieldDidChange() {
        textDidChange?(textField.text)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension AddressCell: UITextFieldDelegate {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        didBeginEditing?()
        contentLabel.isHidden = true
        gr.isEnabled = false
        tapView.isUserInteractionEnabled = false
    }

    func textFieldDidEndEditing(_ textField: UITextField) {
        contentLabel.isHidden = false
        textField.isHidden = true
        gr.isEnabled = true
        tapView.isUserInteractionEnabled = true
        contentLabel.text = textField.text
        
        if let text = textField.text,
           let addressType = Backend.addressResolver.getAddressType(address: text) {
            showResolvingSpinner()
            DispatchQueue.main.async {
                Backend.addressResolver.resolveAddress(addressType: addressType,
                                                       target: text,
                                                       currencyCode: self.currency.code,
                                                       nativeCurrencyCode: self.nativeCurrencyCode) { (result, _) in
                    self.didReceiveResolvedAddress?(result, addressType)
                }
            }
        }
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }

    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if let request = PaymentRequest(string: string, currency: currency) {
            didReceivePaymentRequest?(request)
            return false
        } else {
            return true
        }
    }
}
