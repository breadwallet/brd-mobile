//
//  AmountViewController.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-05-19.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

private let currencyHeight: CGFloat = 80.0
private let feeHeight: CGFloat = 130.0

class AmountViewController: UIViewController, Trackable {
    
    private let currency: Currency
    private var canEditFee: Bool
    
    init(currency: Currency, isPinPadExpandedAtLaunch: Bool, isRequesting: Bool = false) {
        self.currency = currency
        self.isPinPadExpandedAtLaunch = isPinPadExpandedAtLaunch
        self.isRequesting = isRequesting
        if let rate = currency.state?.currentRate, Store.state.showFiatAmounts {
            self.currencyToggle = BRDButton(title: "\(rate.code) (\(rate.currencySymbol))", type: .tertiary)
        } else {
            let title = currency.defaultUnitName
            self.currencyToggle = BRDButton(title: title, type: .tertiary)
        }
        self.feeSelector = FeeSelector(currency: currency)
        self.pinPad = PinPadViewController(style: .white,
                                           keyboardType: .decimalPad,
                                           maxDigits: Int(currency.defaultUnit.decimals),
                                           shouldShowBiometrics: false)
        self.canEditFee = currency.isBitcoin || currency.isEthereumCompatible
        super.init(nibName: nil, bundle: nil)
    }

    // returns Balance Text, Fee Text and isUserInteractionEnabled for balanceLabel
    var balanceTextForAmount: ((Amount?, Rate?) -> (NSAttributedString?, NSAttributedString?, Bool)?)?
    var didUpdateAmount: ((Amount?) -> Void)?
    var didChangeFirstResponder: ((Bool) -> Void)?
    var didTapMax: (() -> Void)?
    var currentOutput: String {
        return amountLabel.text ?? ""
    }
    var selectedRate: Rate? {
        didSet {
            fullRefresh()
        }
    }
    var maximum: Amount?
    var didUpdateFee: ((FeeLevel) -> Void)? {
        didSet {
            feeSelector.didUpdateFee = didUpdateFee
        }
    }
    
    func forceUpdateAmount(amount: Amount) {
        self.amount = amount
        fullRefresh()
    }

    func expandPinPad() {
        if pinPadHeight?.constant == 0.0 {
            togglePinPad()
        }
    }
    
    var isEditable: Bool = true {
        didSet {
            tapView.isUserInteractionEnabled = isEditable
        }
    }

    private let isPinPadExpandedAtLaunch: Bool
    private let isRequesting: Bool
    var minimumFractionDigits = 0
    let balanceLabel = UIButton(type: .system)
    private var hasTrailingDecimal = false
    private var pinPadHeight: NSLayoutConstraint?
    private let placeholder = UILabel(font: .customBody(size: 16.0), color: .grayTextTint)
    private let amountLabel = UILabel(font: .customBody(size: 26.0), color: .darkText)
    private let pinPad: PinPadViewController
    private let currencyToggle: BRDButton
    private let border = UIView(color: .secondaryShadow)
    private let bottomBorder = UIView(color: .secondaryShadow)
    private let cursor = BlinkingView(blinkColor: C.defaultTintColor)
    private let feeLabel = UILabel()
    private let tapView = UIView()
    private let feeSelector: FeeSelector
    private var currentBalanceText: (NSAttributedString?, NSAttributedString?)
    
    //Controlled by SendViewController
    var isSendViewSendingMax = false
    
    private var amount: Amount? {
        didSet {
            updateAmountLabel()
            updateBalanceLabel()
            didUpdateAmount?(amount)
        }
    }

    override func viewDidLoad() {
        addSubviews()
        addConstraints()
        setInitialData()
    }

    private func addSubviews() {
        view.addSubview(amountLabel)
        view.addSubview(placeholder)
        view.addSubview(currencyToggle)
        view.addSubview(feeSelector)
        view.addSubview(border)
        view.addSubview(cursor)
        view.addSubview(feeLabel)
        view.addSubview(tapView)
        view.addSubview(balanceLabel)
        view.addSubview(bottomBorder)
    }

    private func addConstraints() {
        amountLabel.constrain([
            amountLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: C.padding[2]),
            amountLabel.centerYAnchor.constraint(equalTo: currencyToggle.centerYAnchor) ])
        placeholder.constrain([
            placeholder.leadingAnchor.constraint(equalTo: amountLabel.leadingAnchor, constant: 2.0),
            placeholder.topAnchor.constraint(equalTo: view.topAnchor, constant: C.padding[1]) ])
        cursor.constrain([
            cursor.leadingAnchor.constraint(equalTo: amountLabel.trailingAnchor, constant: 2.0),
            cursor.heightAnchor.constraint(equalToConstant: 24.0),
            cursor.centerYAnchor.constraint(equalTo: amountLabel.centerYAnchor),
            cursor.widthAnchor.constraint(equalToConstant: 2.0) ])
        currencyToggle.constrain([
            currencyToggle.topAnchor.constraint(equalTo: view.topAnchor, constant: C.padding[2]),
            currencyToggle.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -C.padding[2]),
            currencyToggle.heightAnchor.constraint(equalToConstant: 35.0)])

        feeSelector.constrain([
            feeSelector.topAnchor.constraint(equalTo: feeLabel.bottomAnchor, constant: 4.0),
            feeSelector.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            feeSelector.trailingAnchor.constraint(equalTo: view.trailingAnchor) ])

        let borderTop = isRequesting
            ? border.topAnchor.constraint(equalTo: currencyToggle.bottomAnchor, constant: C.padding[2])
            : border.topAnchor.constraint(equalTo: feeSelector.bottomAnchor)
        border.constrain([
            border.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            borderTop,
            border.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            border.heightAnchor.constraint(equalToConstant: 1.0) ])
        balanceLabel.constrain([
            balanceLabel.leadingAnchor.constraint(equalTo: amountLabel.leadingAnchor),
            balanceLabel.topAnchor.constraint(equalTo: cursor.bottomAnchor) ])
        feeLabel.constrain([
            feeLabel.leadingAnchor.constraint(equalTo: balanceLabel.leadingAnchor),
            feeLabel.topAnchor.constraint(equalTo: balanceLabel.firstBaselineAnchor, constant: 2.0)])
        pinPadHeight = pinPad.view.heightAnchor.constraint(equalToConstant: 0.0)
        addChildViewController(pinPad, layout: {
            pinPad.view.constrain([
                pinPad.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                pinPad.view.topAnchor.constraint(equalTo: border.bottomAnchor),
                pinPad.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                pinPad.view.bottomAnchor.constraint(equalTo: bottomBorder.topAnchor),
                pinPadHeight ])
        })
        bottomBorder.constrain([
            bottomBorder.topAnchor.constraint(greaterThanOrEqualTo: currencyToggle.bottomAnchor, constant: C.padding[2]),
            bottomBorder.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            bottomBorder.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            bottomBorder.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            bottomBorder.heightAnchor.constraint(equalToConstant: 1.0) ])

        tapView.constrain([
            tapView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tapView.topAnchor.constraint(equalTo: view.topAnchor),
            tapView.trailingAnchor.constraint(equalTo: currencyToggle.leadingAnchor, constant: 4.0),
            tapView.bottomAnchor.constraint(equalTo: feeSelector.topAnchor) ])
        preventAmountOverflow()
    }

    private func setInitialData() {
        cursor.isHidden = true
        cursor.startBlinking()
        amountLabel.text = ""
        placeholder.text = S.Send.amountLabel
        bottomBorder.isHidden = true
        if Store.state.showFiatAmounts {
            if let rate = currency.state?.currentRate {
                selectedRate = rate
            }
        }
        pinPad.ouputDidUpdate = { [weak self] output in
            self?.handlePinPadUpdate(output: output)
        }
        currencyToggle.tap = strongify(self) { myself in
            myself.toggleCurrency()
        }
        let gr = UITapGestureRecognizer(target: self, action: #selector(didTap))
        tapView.addGestureRecognizer(gr)
        tapView.isUserInteractionEnabled = true

        if isPinPadExpandedAtLaunch {
            didTap()
        }

        feeLabel.numberOfLines = 0
        feeLabel.lineBreakMode = .byWordWrapping
        
        if isRequesting || !canEditFee {
            feeSelector.constrain([
                feeSelector.heightAnchor.constraint(equalToConstant: 0)])
        }
        
        balanceLabel.addTarget(self, action: #selector(didTapBalance), for: .touchUpInside)
        balanceLabel.isUserInteractionEnabled = false
    }

    @objc private func didTapBalance() {
        didTapMax?()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: { [weak self] in
            self?.didTap()
        })
    }
    
    override func viewDidLayoutSubviews() {
        // This fullRefresh() call is needed to set the initial view and make sure
        // everything is laid out correctly. A full refresh sets most of the
        // initial values and makes sure the appropriate views are shown/hidden
        // depending on what context this view is used in..
        fullRefresh()
    }

    private func toggleCurrency() {
        saveEvent("amount.swapCurrency")
        selectedRate = selectedRate == nil ? currency.state?.currentRate : nil
        updateCurrencyToggleTitle()
    }
    
    func disableEconomyFee() {
        feeSelector.disableEconomyFee()
    }

    private func preventAmountOverflow() {
        amountLabel.constrain([
            amountLabel.trailingAnchor.constraint(lessThanOrEqualTo: currencyToggle.leadingAnchor, constant: -C.padding[2]) ])
        amountLabel.minimumScaleFactor = 0.5
        amountLabel.adjustsFontSizeToFitWidth = true
        amountLabel.setContentCompressionResistancePriority(UILayoutPriority.defaultLow, for: .horizontal)
    }

    private func handlePinPadUpdate(output: String) {
        let currencyDecimalSeparator = NumberFormatter().currencyDecimalSeparator ?? "."
        placeholder.isHidden = output.utf8.isEmpty ? false : true
        minimumFractionDigits = 0 //set default
        if let decimalLocation = output.range(of: currencyDecimalSeparator)?.upperBound {
            let locationValue = output.distance(from: output.endIndex, to: decimalLocation)
            minimumFractionDigits = abs(locationValue)
        }

        //If trailing decimal, append the decimal to the output
        hasTrailingDecimal = false //set default
        if let decimalLocation = output.range(of: currencyDecimalSeparator)?.upperBound {
            if output.endIndex == decimalLocation {
                hasTrailingDecimal = true
            }
        }

        if let rate = selectedRate {
            amount = Amount(fiatString: output,
                            currency: currency,
                            rate: rate)
        } else {
            amount = Amount(tokenString: output,
                            currency: currency,
                            unit: currency.defaultUnit)
        }
    }

    private func updateAmountLabel() {
        guard let amount = amount else { amountLabel.text = ""; return }
        let displayAmount = Amount(amount: amount,
                                   rate: selectedRate,
                                   minimumFractionDigits: minimumFractionDigits)
        var output = (selectedRate == nil) ? displayAmount.tokenFormattedString : displayAmount.fiatDescription
        if hasTrailingDecimal {
            output = output.appending(NumberFormatter().currencyDecimalSeparator)
        }
        amountLabel.text = output
        placeholder.isHidden = output.utf8.isEmpty ? false : true
        
        if let max = maximum {
            if amount > max {
                amountLabel.textColor = UIColor.cameraGuideNegative
            } else {
                amountLabel.textColor = .darkText
            }
        }
    }

    func updateBalanceLabel() {
        if let (balance, fee, isUserInteractionEnabled) = balanceTextForAmount?(amount, selectedRate) {
            guard balance != nil else { return }
            UIView.performWithoutAnimation {
                self.balanceLabel.setAttributedTitle(balance ?? NSAttributedString(string: " "), for: .normal)
                self.balanceLabel.layoutIfNeeded()
            }
            feeLabel.attributedText = fee
            if amount != nil || isSendViewSendingMax {
                balanceLabel.isHidden = false
                feeLabel.isHidden = false
            } else {
               balanceLabel.isHidden = cursor.isHidden
               feeLabel.isHidden = cursor.isHidden
            }
            balanceLabel.isUserInteractionEnabled = isUserInteractionEnabled
        }
    }

    @objc private func didTap() {
        UIView.spring(C.animationDuration, animations: {
            self.togglePinPad()
            self.parent?.parent?.view.layoutIfNeeded()
        }, completion: { _ in })
    }

    func closePinPad() {
        pinPadHeight?.constant = 0.0
        cursor.isHidden = true
        bottomBorder.isHidden = true
        updateBalanceAndFeeLabels()
        updateBalanceLabel()
    }

    private func togglePinPad() {
        let isCollapsed: Bool = pinPadHeight?.constant == 0.0
        pinPadHeight?.constant = isCollapsed ? pinPad.height : 0.0
        cursor.isHidden = isCollapsed ? false : true
        bottomBorder.isHidden = isCollapsed ? false : true
        updateBalanceAndFeeLabels()
        updateBalanceLabel()
        didChangeFirstResponder?(isCollapsed)
    }

    private func updateBalanceAndFeeLabels() {
        if let amount = amount, !amount.isZero {
            balanceLabel.isHidden = false
        } else {
            balanceLabel.isHidden = cursor.isHidden
        }
    }

    private func fullRefresh() {
        updateCurrencyToggleTitle()
        updateBalanceLabel()
        updateAmountLabel()

        //Update pinpad content to match currency change
        //This must be done AFTER the amount label has updated
        let currentOutput = amountLabel.text ?? ""
        var set = CharacterSet.decimalDigits
        set.formUnion(CharacterSet(charactersIn: NumberFormatter().currencyDecimalSeparator))
        pinPad.currentOutput = String(String.UnicodeScalarView(currentOutput.unicodeScalars.filter { set.contains($0) }))
    }

    private func updateCurrencyToggleTitle() {
        if let rate = selectedRate {
            self.currencyToggle.title = "\(rate.code) (\(rate.currencySymbol))"
        } else {
            currencyToggle.title = currency.defaultUnitName
        }
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
