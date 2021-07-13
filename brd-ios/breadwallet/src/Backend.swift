//
//  Backend.swift
//  breadwallet
//
//  Created by Ehsan Rezaie on 2018-08-15.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import WebKit
import Cosmos

class Backend {
    
    // MARK: - Singleton
    
    private static let shared = Backend()

    private init() {
        let authenticator = NoAuthWalletAuthenticator()
        apiClient = BRAPIClient(authenticator: authenticator)
        bdbClient = BdbServiceCompanion().create()
        bdbClientAuthProvider = CosmosAuthProvider()
        addressResolver = AddressResolver(bdbService: bdbClient, isMainnet: !E.isTestnet)
    }
    
    // MARK: - Private
    
    private var apiClient: BRAPIClient
    private var bdbClient: BdbService
    private var bdbClientAuthProvider: BdbServiceAuthProvider
    private var kvStore: BRReplicatedKVStore?
    private var exchangeUpdater: ExchangeUpdater?
    private var eventManager: EventManager?
    private var addressResolver: AddressResolver
    private let userAgentFetcher = UserAgentFetcher()
    
    // MARK: - Public
    
    static var isConnected: Bool {
        return (apiClient.authKey != nil)
    }
    
    static var apiClient: BRAPIClient {
        return shared.apiClient
    }
    
    static var bdbClient: BdbService {
        return shared.bdbClient
    }
    
    static var addressResolver: AddressResolver {
        return shared.addressResolver
    }
    
    static var kvStore: BRReplicatedKVStore? {
        return shared.kvStore
    }

    static var eventManager: EventManager? {
        return shared.eventManager
    }
    
    static func updateExchangeRates() {
        shared.exchangeUpdater?.refresh()
    }
    
    static func sendLaunchEvent() {
        DispatchQueue.main.async { // WKWebView creation must be on main thread
            shared.userAgentFetcher.getUserAgent { userAgent in
                shared.apiClient.sendLaunchEvent(userAgent: userAgent)
            }
        }
    }
    
    // MARK: Setup
    
    static func connect(authenticator: WalletAuthenticator) {
        guard let key = authenticator.apiAuthKey else { return assertionFailure() }
        shared.apiClient = BRAPIClient(authenticator: authenticator)
        shared.kvStore = try? BRReplicatedKVStore(encryptionKey: key, remoteAdaptor: KVStoreAdaptor(client: shared.apiClient))
        shared.exchangeUpdater = ExchangeUpdater()
        shared.eventManager = EventManager(adaptor: shared.apiClient)
        shared.bdbClientAuthProvider = CosmosAuthProvider(authenticator: authenticator)
        shared.bdbClient = BdbServiceCompanion().create(authProvider: shared.bdbClientAuthProvider)
        shared.addressResolver = AddressResolver(bdbService: shared.bdbClient, isMainnet: !E.isTestnet)
    }
    
    /// Disconnect backend services and reset API auth
    static func disconnectWallet() {
        URLCache.shared.removeAllCachedResponses()
        shared.eventManager = nil
        shared.exchangeUpdater = nil
        shared.kvStore = nil
        shared.apiClient = BRAPIClient(authenticator: NoAuthWalletAuthenticator())
        shared.bdbClient = BdbServiceCompanion().create()
        shared.addressResolver = AddressResolver(bdbService: shared.bdbClient, isMainnet: !E.isTestnet)
    }
}

// MARK: - 

class UserAgentFetcher {
    lazy var webView: WKWebView = { return WKWebView(frame: .zero) }()
    
    func getUserAgent(completion: @escaping (String) -> Void) {
        webView.loadHTMLString("<html></html>", baseURL: nil)
        webView.evaluateJavaScript("navigator.userAgent") { (result, error) in
            guard let agent = result as? String else {
                print(String(describing: error))
                return completion("")
            }
            completion(agent)
        }
    }
}

@objc class CosmosAuthProvider: NSObject, BdbServiceAuthProvider {
    let authenticator: WalletAuthenticator?
    
    init(authenticator: WalletAuthenticator? = nil) {
        self.authenticator = authenticator
    }
    func doNewTokenDetails() -> BdbServiceAuthProviderTokenDetails {
        return BdbServiceAuthProviderTokenDetails(currentTimeSeconds: 0, expirationTimeSeconds: 0)
    }
    
    func readDeviceId() -> String {
        return ""
    }
    
    func readPubKey() -> String {
        return ""
    }
    
    func saveUserJwt(jwt: String) {
        return
    }
    
    func signData(data: String) -> String {
        return ""
    }
    
    /// Return User-level JWT, or general Client JWT if User JWT hasn't been retrieved yet
    func readUserJwt() -> String? {
        return authenticator?.bdbAuthToken?.token ?? ""
    }
    
    func readClientJwt() -> String? {
        return ""
    }
}
