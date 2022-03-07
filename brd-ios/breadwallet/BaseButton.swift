// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol BaseButtonRequirements {
    func updateUIForState()
}

class BaseButton: UIButton, BaseButtonRequirements {
    override var isSelected: Bool {
        didSet {
            updateUIForState()
        }
    }
    
    override var isHighlighted: Bool {
        didSet {
            updateUIForState()
        }
    }
    
    func updateUIForState() { }
}
