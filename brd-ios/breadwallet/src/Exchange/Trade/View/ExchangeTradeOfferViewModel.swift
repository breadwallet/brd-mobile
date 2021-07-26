//
//  OfferViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

enum ExchangeTradeOfferViewModel {
    case offer(offer: Offer?)
    case noOffer(noOffer: CellLayoutView.ViewModel)
    case loading

    static func from(
        _ model: ExchangeModel,
        onTap: (() -> Void)? = nil
    ) -> ExchangeTradeOfferViewModel {
        switch model.offerState {
        case .noOffers:
            return .noOffer(noOffer: ExchangeTradeOfferViewModel.noOffer())
        case .gathering:
            return .loading
        default:
            let offer = model.selectedOffer
            
            guard (offer as? ExchangeModel.OfferDetailsValidOffer) != nil else {
                return .offer(offer: nil)
            }

            return .offer(
                offer: .init(model, onTap: onTap)
            )
        }
    }

    func isNoOffer() -> Bool {
        switch self {
        case .noOffer:
            return true
        default:
            return false
        }
    }
}

// MARK: - OfferViewModel.Offer

extension ExchangeTradeOfferViewModel {

    struct Offer {
        let icon: UIImage?
        let iconURL: URL?
        let name: String
        let rate: String
        let fees: String
        let tap: (() -> Void)?
    }
}

extension ExchangeTradeOfferViewModel.Offer {

    init?(
        _ model: ExchangeModel?,
        onTap: (() -> Void)? = nil
    ) {
        guard let offer = model?.selectedOffer else {
            return nil
        }

        let invoice = offer.offer.invoiceEstimate
        let url = offer.offer.provider.logoUrl
        let valid = offer as? ExchangeModel.OfferDetailsValidOffer
        let invalid = offer as? ExchangeModel.OfferDetailsInvalidOffer
        let slug = offer.offer.provider.slug
        let sourceFees = (try? invoice?.sourceCurrency.fees.double()) ?? 0 != 0

        iconURL = url == nil ? nil : URL(string: url ?? "")
        name = offer.offer.provider.name
        rate = valid?.formattedSourceRatePerQuote ?? ""
        tap = onTap

        icon = UIImage(named: slug)
            ?? UIImage(named: slug.replacingOccurrences(of: "-test", with: ""))
            ?? UIImage(named: "unknownProvider")

        fees = sourceFees
            ? valid?.formattedSourceFees ?? ""
            : valid?.formattedQuoteFees ?? ""
    }
}

// MARK: - No offer

private extension ExchangeTradeOfferViewModel {

    static func noOffer() -> CellLayoutView.ViewModel {
        return .init(
            title: S.Exchange.Offer.noneTitle,
            subtitle: S.Exchange.Offer.noneSubtitle,
            iconImage: UIImage(named: "OfferNone")
        )
    }
}
