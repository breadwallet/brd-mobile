// 
//  PairPickerViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangePairPickerViewModel {
    let formIcon: UIImage?
    let toIcon: UIImage?
    let isSelectingFrom: Bool
    let pickerViewModel: PickerViewModel
    let fromAction: (() -> Void)?
    let toAction: (() -> Void)?
}

// MARK: - Initializers

extension ExchangePairPickerViewModel {

    init?(
        _ model: ExchangeModel,
        collection: AssetCollection?,
        consumer: TypedConsumer<ExchangeEvent>?
    ) {
        guard let state = model.state as? ExchangeModel.StateSelectAsset else {
            return nil
        }

        guard let viewModel = PickerViewModel(
            currencyPickerFrom: model,
            assetCollection: collection,
            consumer: consumer,
            showFiatRates: false,
            filterZeroBalances: state.source
        ) else {
            return nil
        }

        let assetCurrencies = collection?.allAssets ?? [:]
        let pair = model.selectedPair
        let fromCode = pair?.fromCode ?? model.sourceCurrencyCode ?? ""
        let toCode = pair?.toCode ?? ""
        let fromId = model.currencies[fromCode]
        let toId = model.currencies[toCode]

        formIcon = assetCurrencies[CurrencyId(rawValue: fromId?.currencyId ?? "")]?
            .imageSquareBackground
        toIcon  = assetCurrencies[CurrencyId(rawValue: toId?.currencyId ?? "")]?
            .imageSquareBackground
        isSelectingFrom = state.source
        pickerViewModel = viewModel
        fromAction = { consumer?.accept(.OnSelectPairClicked(selectSource: true)) }
        toAction = { consumer?.accept(.OnSelectPairClicked(selectSource: false)) }
    }
}
