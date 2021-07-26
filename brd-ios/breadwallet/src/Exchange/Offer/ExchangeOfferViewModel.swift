//
//  ExchangeOfferViewModel.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

struct ExchangeOfferViewModel: Equatable {
    
    let title: String
    let sectionTitle: String?
    let offers: [Offer]
    let selectedAction: ((Int) -> Void)?
    let minMaxAction: ((Int) -> Void)?
    let closeAction: (() -> Void)?
    
    static func ==(
        lhs: ExchangeOfferViewModel,
        rhs: ExchangeOfferViewModel
    ) -> Bool {
        return rhs.title == lhs.title &&
        rhs.sectionTitle == lhs.sectionTitle &&
        rhs.offers == lhs.offers
    }
}

// MARK: - ExchangeOfferViewModel.Offer

extension ExchangeOfferViewModel {

    struct Offer: Equatable {
        let title: String
        let subtitle: String
        let iconURL: URL?
        let iconImage: UIImage?
        let selected: Bool
        let rate: String
        let feesTotal: String
        let showFeesBreakDown: Bool
        let fees: [Fee]
        let totalQuote: String
        let totalBase: String
        let isValid: Bool
        let ctaTitle: String?

        struct Fee: Equatable {
            let title: String
            let amount: String
        }
    }
}

// MARK: - ExchangeModel.Offer

extension ExchangeOfferViewModel {

    init(model: ExchangeModel, consumer: TypedConsumer<ExchangeEvent>?) {
        title = S.Exchange.Offer.offers
        sectionTitle = S.Exchange.Offer.sectionTitle
        let offers = model.offerDetails

        let validOffers: [ExchangeOfferViewModel.Offer] = offers
            .compactMap { $0 as? ExchangeModel.OfferDetailsValidOffer }
            .map {
                let fees: [ExchangeOfferViewModel.Offer.Fee] = [
                    .init(
                        title: S.Exchange.Offer.processingFee,
                        amount: $0.formattedProviderFee ?? ""
                    ),
                    .init(
                        title: S.Exchange.Offer.networkFee,
                        amount: $0.formattedNetworkFee ?? ""
                    ),
                    .init(
                        title: S.Exchange.Offer.providerFee,
                        amount: $0.formattedProviderFee ?? ""
                    )
                ]

                return .init(
                    title: ExchangeOfferViewModel.title(for: $0),
                    subtitle: ExchangeOfferViewModel.subtitle(offer: $0),
                    iconURL: URL(string: $0.offer.provider.logoUrl ?? ""),
                    iconImage: UIImage(named: $0.offer.provider.imageSlug),
                    selected: $0 == model.selectedOffer,
                    rate: $0.formattedSourceRate ?? "",
                    feesTotal: $0.formattedSourceFees ?? "",
                    showFeesBreakDown: model.mode == .trade,
                    fees: fees,
                    totalQuote: $0.formattedSourceTotal,
                    totalBase: $0.formattedQuoteTotal,
                    isValid: true,
                    ctaTitle: nil
                )
            }

        let invalidOffers: [ExchangeOfferViewModel.Offer] = offers
            .compactMap { $0 as? ExchangeModel.OfferDetailsInvalidOffer }
            .map {
                return .init(
                    title: ExchangeOfferViewModel.title(for: $0),
                    subtitle: ExchangeOfferViewModel.subtitle(offer: $0),
                    iconURL: URL(string: $0.offer.provider.logoUrl ?? ""),
                    iconImage: UIImage(named: $0.offer.provider.imageSlug),
                    selected: $0 == model.selectedOffer,
                    rate: "",
                    feesTotal: "",
                    showFeesBreakDown: model.mode == .trade,
                    fees: [],
                    totalQuote: "",
                    totalBase: "",
                    isValid: false,
                    ctaTitle: ExchangeOfferViewModel.ctaTitle(model, offer: $0)
                )
            }

        self.offers = validOffers + invalidOffers
        selectedAction = {
            let event = ExchangeEvent.OnOfferClicked(
                    offerDetails: offers[$0],
                    adjustToLimit: false
            )
            consumer?.accept(event)
        }
        minMaxAction = {
            let event = ExchangeEvent.OnOfferClicked(
                    offerDetails: offers[$0],
                    adjustToLimit: true
            )
            consumer?.accept(event)
        }
        closeAction = { consumer?.accept(.OnBackClicked()) }
    }
}

// MARK: - Utilities

private extension ExchangeOfferViewModel {

    static func title(for offer: ExchangeModel.OfferDetails) -> String {
        return String(format: offer.formattedViaMethod(), offer.offer.provider.name ?? "")
    }

    static func subtitle(offer: ExchangeModel.OfferDetails) -> String {
        guard let offer = offer as? ExchangeModel.OfferDetailsInvalidOffer else {
            return offer.offer.provider.slug
        }

        if let min = offer.formattedMinSourceAmount {
            return S.Exchange.Offer.limitMin + " \(min)"
        }

        if let max = offer.formattedMaxSourceAmount {
            return S.Exchange.Offer.limitMax + " \(max)"
        }

        return offer.offer.provider.imageSlug
    }

    static func ctaTitle(
        _ model: ExchangeModel,
        offer: ExchangeModel.OfferDetailsInvalidOffer
    ) -> String? {

        if let minAmount = offer.minSourceAmount,
           minAmount.doubleValue > model.sourceAmount {
            let minSourceString = offer.formattedMinSourceAmount ?? ""
            return String(format: S.Exchange.CTA.setMin, minSourceString)
        }

        if let maxAmount = offer.maxSourceAmount,
           maxAmount.doubleValue < model.sourceAmount {
            let maxSourceString = offer.formattedMaxSourceAmount ?? ""
            return String(format: S.Exchange.CTA.setMax, maxSourceString)
        }

        return nil
    }
}
