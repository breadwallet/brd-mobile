// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

/// For setting variables (non static content)
protocol GenericSettable {
    associatedtype Model
    func setup(with model: Model)
}
