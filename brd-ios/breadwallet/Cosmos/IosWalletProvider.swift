// 
//  IosWalletProvider.swift
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

class IosWalletProvider: WalletProvider {
    
    private let system: CoreSystem

    init(system: CoreSystem) {
        self.system = system
    }
    
    func loadWalletBalances() -> [String: KotlinDouble] {
        let balances = system.wallets.mapValues { wallet -> (String, KotlinDouble) in
            let currencyCode = wallet.currency.code.lowercased(with: .none)
            let coreBalance = wallet.balanceMaximum?.cryptoAmount ?? wallet.balance.cryptoAmount
            let balance = coreBalance.double(as: wallet.currency.defaultUnit) ?? 0.0
            return (currencyCode, KotlinDouble(double: balance))
        }

        return Dictionary(uniqueKeysWithValues: balances.values)
    }
    
    func enableWallet(currencyId: String) {
        let id = CurrencyId(rawValue: currencyId.adjustId())
        if system.assetCollection?.isEnabled(id) == false {
            if let asset = system.assetCollection?.allAssets[id] {
                system.assetCollection?.add(asset: asset)
                system.assetCollection?.saveChanges()
            }
        }
    }
    
    func receiveAddressFor(currencyId: String) -> String? {
        let adjustedCurrencyId: String = currencyId.adjustId()
        let wallet = system.wallets[CurrencyId(rawValue: adjustedCurrencyId)]

        if wallet == nil {
            enableWallet(currencyId: adjustedCurrencyId)
        }

        if wallet?.currency.isBitcoin ?? false {
            return wallet?.receiveAddress(for: .btcLegacy)
        }

        return wallet?.receiveAddress
    }
    
    func estimateLimitMaximum(currencyId: String, targetAddress: String) -> KotlinDouble? {
        let currencyId = CurrencyId(rawValue: currencyId.adjustId())
        guard let wallet = system.wallets[currencyId] else {
            return nil
        }

        let group = DispatchGroup()
        var result: Result<WalletKit.Amount, WalletKit.Wallet.LimitEstimationError>?
        group.enter()
        wallet.estimateLimitMaximum(address: targetAddress, fee: .priority) {
            result = $0
            group.leave()
        }
        group.wait()

        switch result {
        case .success(let maxAmount):
            let maxDouble = maxAmount.double(as: wallet.currency.defaultUnit)
            return KotlinDouble(double: maxDouble!)
        case .failure(let error):
            print("[LIMIT] error: \(error)")
            return nil
        case .none:
            return nil
        }
    }

    func currencyCode(currencyId: String) -> String? {
        let currencyId = CurrencyId(rawValue: currencyId.adjustId())
        return system.wallets[currencyId]?.currency.code
    }

    func networkCurrencyCode(currencyId: String) -> String? {
        let currencyId = CurrencyId(rawValue: currencyId.adjustId())
        return system.wallets[currencyId]?.networkCurrency?.code
    }

    func estimateFee(currencyId: String, targetAddress: String) -> KotlinDouble? {
        let currencyId = CurrencyId(rawValue: currencyId.adjustId())
        let group = DispatchGroup()
        var feeBases: TransferFeeBasis?

        guard let wallet = system.wallets[currencyId] else {
            return nil
        }

        group.enter()
        wallet.estimateFee(
            address: targetAddress,
            amount: wallet.balanceMaximum ?? wallet.balance,
            fee: .priority,
            isStake: false,
            completion: { feeBases = $0 }
        )
        group.wait()

        return KotlinDouble(
            double: feeBases?.fee.double(as: wallet.currency.defaultUnit) ?? 0
        )
    }
}

fileprivate extension String {

    func adjustId() -> String {
        if E.isTestnet {
            return replacingOccurrences(of: "ethereum-mainnet", with: "ethereum-ropsten")
                .replacingOccurrences(of: "mainnet", with: "testnet")
        } else {
            return self
        }
    }
}
