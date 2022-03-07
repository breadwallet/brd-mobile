// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCCompleteBusinessLogic {
    // MARK: Business logic functions
}

protocol KYCCompleteDataStore {
    // MARK: Data store
}

class KYCCompleteInteractor: KYCCompleteBusinessLogic, KYCCompleteDataStore {
    var presenter: KYCCompletePresentationLogic?
    
    // MARK: Interactor functions

}
