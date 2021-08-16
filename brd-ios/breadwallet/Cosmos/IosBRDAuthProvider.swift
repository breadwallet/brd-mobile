//
//  IosBrdAuthProvider.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos
import WalletKit

class IosBrdAuthProvider: BrdAuthProviderBase {
    
    private var authenticator: WalletAuthenticator
    
    init(walletAuthenticator: WalletAuthenticator) {
        self.authenticator = walletAuthenticator
    }
    
    private var authKey: Key? {
        if authenticator.noWallet { return nil }
        let key = authenticator.apiAuthKey
        assert(key != nil)
        return key
    }
    
    override var token: String? {
        get {
            let tokenData = authenticator.apiUserAccount ?? [:]
            return tokenData["token"] as? String
        }
        set {
            var tokenData = authenticator.apiUserAccount ?? [:]
            tokenData["token"] = newValue
            authenticator.apiUserAccount = tokenData 
        }
    }
    
    override func deviceId() -> String {
        return UserDefaults.deviceID
    }
    
    override func hasKey() -> Bool {
        return authenticator.apiAuthKey != nil
    }
    
    override func publicKey() -> String {
        return authenticator.apiAuthKey!.encodeAsPublic.hexToData!.base58
    }

    override func walletId() -> String? {
        return Store.state.walletID
    }
    
    override func sign(method: String, body: String, contentType: String, url: String) -> BrdAuthProviderSignature {
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
        return BrdAuthProviderSignature.init(signature: sig.base58, timestamp: date)
    }

    func signedGetUrl(host: String, path: String) -> URL? {
        let signature = sign(method: "GET", body: "", contentType: "", url: path)
        var urlComponents = URLComponents()
        urlComponents.scheme = "https"
        urlComponents.host = host.replacingOccurrences(of: "https://", with: "")
        urlComponents.path = path
        urlComponents.queryItems = [
            URLQueryItem(name: "Authorization", value: authorization(signature: signature.signature)),
            URLQueryItem(name: "Date", value: signature.timestamp),
        ]
        return urlComponents.url
    }
}
