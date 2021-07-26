#!/usr/bin/swift

import Foundation

private let baseString = "https://bwsupport.zendesk.com/api/v2/help_center/en-us"
private let outputURL = URL(fileURLWithPath: "../brd-ios/breadwallet/Resources/support")
private let decoder = JSONDecoder()
private let encoder = JSONEncoder()

struct ArticlesContainer: Codable {
    let articles: [Article]
}

struct SectionsContainer: Codable {
    let sections: [Section]
}

struct Article: Codable {
    let id: UInt
    let title: String
    let sectionId: UInt
    let promoted: Bool
    let position: Int
    let labelNames: [String]
    let body: String
    
    enum CodingKeys: String, CodingKey {
        case id
        case title
        case sectionId = "section_id"
        case promoted
        case position
        case labelNames = "label_names"
        case body
    }
}

struct Section: Codable {
    let id: UInt
    let title: String
    let position: UInt
       
    enum CodingKeys: String, CodingKey {
        case id
        case title = "name"
        case position
    }
}

func downloadAndProcess(_ url: URL?, to fileURL: URL?, isArticle: Bool) {
    guard let url = url, let fileURL = fileURL else {
        print("Nil url")
        return
    }
    do {
        var data = try Data(contentsOf: url)
        if isArticle {
            let container = try decoder.decode(ArticlesContainer.self, from: data)
            data = try encoder.encode(container.articles)
            try data.write(to: fileURL)
        } else {
            let container = try decoder.decode(SectionsContainer.self, from: data)
            data = try encoder.encode(container.sections)
            try data.write(to: fileURL)
        }
    } catch {
        print(error, url, fileURL)
    }
}

downloadAndProcess(
    URL(string: "\(baseString)/categories/360000438414/articles.json?per_page=100"),
    to: outputURL.appendingPathComponent("secondary_articles.json"),
    isArticle: true
)
downloadAndProcess(
    URL(string: "\(baseString)/categories/360000437273/articles.json?per_page=100"),
    to: outputURL.appendingPathComponent("main_articles.json"),
    isArticle: true
)
downloadAndProcess(
    URL(string: "\(baseString)/categories/360000437273/sections.json?per_page=200"),
    to: outputURL.appendingPathComponent("sections.json"),
    isArticle: false
)
