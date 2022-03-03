// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCCompleteButtons: UITableViewCell {
    private lazy var doneButton: KYCButton = {
        let doneButton = KYCButton()
        doneButton.translatesAutoresizingMaskIntoConstraints = false
        doneButton.setup(as: .normal, title: "DONE")
        
        return doneButton
    }()
    
    var didTapDoneButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        let defaultDistance: CGFloat = 12
        
        addSubview(doneButton)
        doneButton.topAnchor.constraint(equalTo: topAnchor, constant: defaultDistance).isActive = true
        doneButton.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 40).isActive = true
        doneButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -40).isActive = true
        doneButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        doneButton.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        
        doneButton.didTap = { [weak self] in
            self?.didTapDoneButton?()
        }
    }
}
