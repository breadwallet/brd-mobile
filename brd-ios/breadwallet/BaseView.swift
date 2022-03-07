// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class BaseView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupSubviews()
        localize()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        
        setupSubviews()
        localize()
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        setupSubviews()
        localize()
    }
    
    func setupSubviews() {}
    
    func localize() {}
}
