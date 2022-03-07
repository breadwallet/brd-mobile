// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCCenteredTextCell: UITableViewCell {
    private lazy var label: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        label.textColor = .kycGray1
        label.font = UIFont(name: "AvenirNext-Regular", size: 14)
        label.numberOfLines = 0
        
        return label
    }()
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(label)
        label.topAnchor.constraint(equalTo: topAnchor, constant: 4).isActive = true
        label.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -4).isActive = true
        label.widthAnchor.constraint(equalTo: widthAnchor, multiplier: 0.7).isActive = true
        label.constrainToCenter()
    }
    
    func setText(_ text: String) {
        label.text = text
    }
}
