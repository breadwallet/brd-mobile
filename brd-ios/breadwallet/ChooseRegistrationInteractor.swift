//
//  ChooseRegistrationInteractor.swift
//  breadwallet
//
//  Created by Dijana Angelovska on 7.3.22.
//  Copyright (c) 2022 Breadwinner AG. All rights reserved.
//

import UIKit

protocol ChooseRegistrationBusinessLogic {
    // MARK: Business logic functions
}

protocol ChooseRegistrationDataStore {
    // MARK: Data store
}

class ChooseRegistrationInteractor: ChooseRegistrationBusinessLogic, ChooseRegistrationDataStore {
    var presenter: ChooseRegistrationPresentationLogic?
    var worker: ChooseRegistrationWorker?

    // MARK: Interactor functions

}
