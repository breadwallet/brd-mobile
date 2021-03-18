//
//  IosBRDAuthProvider.swift
//  breadwallet
//
//  Created by Andrew Carlson on 2/25/21.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import Cosmos
import WalletKit

class IosBRDAuthProvider: BRDAuthProvider {
    
    private let authenticator: WalletAuthenticator
    
    init(walletAuthenticator: WalletAuthenticator) {
        self.authenticator = walletAuthenticator
    }
    
    private var authKey: Key? {
        if authenticator.noWallet { return nil }
        let key = authenticator.apiAuthKey
        assert(key != nil)
        return key
    }
    
    var token: String? {
        get {
            let tokenData = authenticator.apiUserAccount!
            return tokenData["token"] as? String
        }
        set {
            var tokenData = authenticator.apiUserAccount!
            tokenData["token"] = newValue
        }
    }
    
    func deviceId() -> String {
        return UserDefaults.deviceID
    }
    
    func hasKey() -> Bool {
        return authenticator.apiAuthKey != nil
    }
    
    func publicKey() -> String {
        return authenticator.apiAuthKey!.encodeAsPublic.hexToData!.base58
    }
    
    func sign(method: String, body: String, contentType: String, url: String) -> BRDAuthProviderSignature {
        let date = Date().RFC1123String()!
        var bodySignature: String = ""
        switch method {
        case "POST", "PUT", "PATCH":
            if !body.isEmpty {
                bodySignature = body.data(using: .utf8)!.sha256.base58
            }
        default: break
        }
        let parts: [String] = [ method, bodySignature, contentType, date, url ]
        let signingString = parts.joined(separator: "\n")
        
        let signingData = signingString.data(using: .utf8)!
        let sig = signingData.sha256_2.compactSign(key: authKey!)!
        return BRDAuthProviderSignature.init(signature: sig.base58, timestamp: date)
    }
}
