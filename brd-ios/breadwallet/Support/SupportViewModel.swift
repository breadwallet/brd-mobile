// 
//  SupportViewModel.swift
//  breadwallet
//
//  Created by blockexplorer on 06/07/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import Cosmos

struct SupportViewModel {
    let sections: [Section]
    let articles: [Article]
    let faqArticles: [Article]
    let searchResults: [Article]
    let selectedSection: Section?
    let selectedArticle: Article?
    let backAction: (() -> Void)?
    let closeAction: (() -> Void)?
    let selectSectionAction: ((_ id: Int64) -> Void)?
    let selectArticleAction: ((_ id: Int64) -> Void)?
    let searchAction: ((_ term: String) -> Void)?
}

// MARK: - Section

extension SupportViewModel {

    struct Section {
        let id: Int64
        let title: String
    }
}

// MARK: - Article

extension SupportViewModel {

    struct Article {
        let id: Int64
        let sectionId: Int64
        let title: String
        let body: String
    }

}

// MARK: - SupportModel convenience initializer

extension SupportViewModel {
    
    init(model: SupportModel, consumer: TypedConsumer<SupportEvent>) {
        self.init(
            sections: model.sections.map { .init(model: $0) }.compactMap { $0 },
            articles: model.articles.map { .init(model: $0) }.compactMap { $0 },
            faqArticles: model.faqArticles.map { .init(model: $0) }.compactMap { $0 },
            searchResults: (model.state as? SupportModel.StateSearch)?.results
                    .map { .init(model: $0) }.compactMap { $0 } ?? [],
            selectedSection: .init(model: model.selectedSection),
            selectedArticle: .init(model: model.selectedArticle),
            backAction: { consumer.accept(.OnBackClicked()) },
            closeAction: { consumer.accept(.OnCloseClicked()) },
            selectSectionAction: { id in
                guard let section = model.sections.first(where: { $0.id == id }) else {
                    return
                }
                consumer.accept(.OnSectionClicked(section: section))
            },
            selectArticleAction: { id in
                guard let article = model.articles.first(where: { $0.id == id }) else {
                    return
                }
                consumer.accept(.OnArticleClicked(article: article))
            },
            searchAction: { consumer.accept(.OnSearch(term: $0)) }
        )
    }
}

// MARK: - SupportModel.Section convenience initializer

extension SupportViewModel.Section {

    init?(model: SupportModel.Section?) {
        guard let model = model else {
            return nil
        }

        self.init(id: model.id, title: model.title)
    }
}

// MARK: - SupportModel.Article convenience initializer

extension SupportViewModel.Article {

    init?(model: SupportModel.Article?) {
        guard let model = model else {
            return nil
        }

        self.init(
            id: model.id,
            sectionId: model.sectionId,
            title: model.title,
            body: model.body
        )
    }
}

// MARK: - Mocks

extension SupportViewModel {

    static func empty() -> SupportViewModel {
        return .init(
            sections: [],
            articles: [],
            faqArticles: [],
            searchResults: [],
            selectedSection: nil,
            selectedArticle: nil,
            backAction: nil,
            closeAction: nil,
            selectSectionAction: nil,
            selectArticleAction: nil,
            searchAction: nil
        )
    }

    static func mockIndex() -> SupportViewModel {
        return .init(
            sections: [
                .init(
                    id: 1,
                    title: "What is a recovery phrase"
                ),
                .init(
                    id: 2,
                    title: "Buys and sell"
                ),
                .init(
                    id: 3,
                    title: "Send and recieve"
                ),
                .init(
                    id: 4,
                    title: "BRD rewards"
                )
            ],
            articles: [
                .init(
                    id: 1,
                    sectionId: 2,
                    title: "Article about buy and sell",
                    body: "This is article about buys and sell. Lorem ipsum."
                ),
                .init(
                    id: 2,
                    sectionId: 2,
                    title: "Another article about buys and sell",
                    body: "This is another article about buys and sell. Lorem ipsum."
                ),
                .init(
                    id: 3,
                    sectionId: 2,
                    title: "Buys and sell bitcoin",
                    body: "This is article about buys and sell bitcoin. Lorem ipsum."
                ),
                .init(
                    id: 4,
                    sectionId: 2,
                    title: "BRD rewards",
                    body: "This is article about rewards. Lorem ipsum."
                )
            ],
            faqArticles: [
                .init(
                    id: 5,
                    sectionId: 1,
                    title: "Recovery phrase articel",
                    body: "This is article about recovery phrase. Lorem ipsum."
                )
            ],
            searchResults: [],
            selectedSection: nil,
            selectedArticle: nil,
            backAction: nil,
            closeAction: nil,
            selectSectionAction: nil,
            selectArticleAction: nil,
            searchAction: nil
        )
    }
}
