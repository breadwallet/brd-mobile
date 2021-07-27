//
//  ExchangeWebViewController.swift
//  breadwallet
// 
//  Created by blockexplorer on 23/04/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit
import WebKit
import MessageUI

class WebViewController: UIViewController {

    typealias Action = () -> Void

    enum NativeDestination: String, CaseIterable {
        case trade = "native/trade"
    }

    var nativeDestinationAction: ((NativeDestination) -> Void)?
    var closeAction: Action?
    var flowEndUrlComponents: [String] = []
    var flowEndedAction: ((_ url: URL?) -> Void)?
    var transparentBg: Bool = false

    private var back: UIBarButtonItem?
    private var forward: UIBarButtonItem?
    private var refresh: UIBarButtonItem?

    private lazy var webView = WKWebView()

    init() {
        super.init(nibName: nil, bundle: nil)
        closeAction = { [weak self] in self?.dismiss() }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupBarButtons()
    }

    func load(_ url: URL?) {
        guard let url = url else {
            return
        }
        load(URLRequest(url: url))
    }

    func load(_ request: URLRequest) {
        webView.load(request)
    }

    static func embeddedInNavigationController(
        _ style: WebViewController.Style = .default,
        showToolbar: Bool = true
    ) -> (UINavigationController, WebViewController) {
        let vc = WebViewController()

        switch style {
        case .brd:
            let navVc = UINavigationController(darkWith: vc)
            navVc.setToolbarHidden(!showToolbar, animated: false)
            navVc.toolbar.barTintColor = Theme.secondaryBackground
            navVc.toolbar.tintColor = Theme.primaryText
            navVc.toolbar.barStyle = .blackOpaque
            navVc.toolbar.isTranslucent = false
            return (navVc, vc)
        default:
            let navVc = UINavigationController(rootViewController: vc)
            navVc.setToolbarHidden(false, animated: false)
            return (navVc, vc)
        }
    }

    static func cleanAllCookies() {
        HTTPCookieStorage.shared.removeCookies(since: Date.distantPast)

        WKWebsiteDataStore.default().fetchDataRecords(
            ofTypes: WKWebsiteDataStore.allWebsiteDataTypes()
        ) { records in
            records.forEach { record in
                WKWebsiteDataStore.default().removeData(
                    ofTypes: record.dataTypes,
                    for: [record],
                    completionHandler: {}
                )
            }
        }
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension WebViewController: WKUIDelegate {

    func webView(
        _ webView: WKWebView,
        createWebViewWith configuration: WKWebViewConfiguration,
        for navigationAction: WKNavigationAction,
        windowFeatures: WKWindowFeatures
    ) -> WKWebView? {
        let url = navigationAction.request.url

        if isClose(url) {
            flowEndedAction?(url)
            return nil
        }

        if isMail(url) {
            let address = url?.absoluteString.replacingOccurrences(
                of: (url?.scheme ?? "") + ":",
                with: ""
            )
            presentMailCompose(emailAddress: address ?? "support@brd.com")
            return nil
        }

        if navigationAction.targetFrame == nil {
            webView.load(navigationAction.request)
        }

        return nil
    }
}

extension WebViewController: WKNavigationDelegate {

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        updateNavigationButtons()

        if isClose(webView.url) {
            flowEndedAction?(webView.url)
        }

        if isMail(webView.url) {
            let address = webView.url?.absoluteString.replacingOccurrences(
                of: (webView.url?.scheme ?? "") + ":",
                with: ""
            )
            presentMailCompose(emailAddress: address ?? "support@brd.com")
        }

        if !transparentBg {
            webView.backgroundColor = .white
        }
    }

    func webView(
        _ webView: WKWebView,
        decidePolicyFor navigationAction: WKNavigationAction,
        decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
    ) {
        let url = navigationAction.request.url

        if isClose(url) {
            decisionHandler(.cancel)
            flowEndedAction?(url)
            return
        }

        if isMail(url) {
            decisionHandler(.cancel)
            let address = url?.absoluteString.replacingOccurrences(
                of: (url?.scheme ?? "") + ":",
                with: ""
            )
            presentMailCompose(emailAddress: address ?? "support@brd.com")
            return
        }

        if let destination = nativeDestination(navigationAction.request.url) {
            decisionHandler(.cancel)
            nativeDestinationAction?(destination)
            return
        }

        decisionHandler(.allow)
    }

    func isClose(_ url: URL?) -> Bool {
        guard let url = url else {
            return false
        }

        for closeURLComponent in flowEndUrlComponents {
            if url.lastPathComponent == closeURLComponent {
                return true
            }
        }

        return false
    }

    func isMail(_ url: URL?) -> Bool {
        return url?.scheme == "mailto"
    }

    func nativeDestination(_ url: URL?) -> NativeDestination? {
        for destination in NativeDestination.allCases {
            if url?.absoluteString.contains(destination.rawValue) ?? false {
                return destination
            }
        }

        return nil
    }
}

// MARK: - Style

extension WebViewController {

    enum Style {
        case `default`
        case brd
    }
}

private extension WebViewController {

    func setupUI() {
        let indicator = UIActivityIndicatorView(style: .whiteLarge)
        view.addSubview(indicator)
        indicator.constrainToCenter()
        indicator.startAnimating()
        webView.backgroundColor = .clear
        webView.isOpaque = false
        view.addSubview(webView)
        webView.constrain(toSuperviewEdges: nil)
        webView.navigationDelegate = self
        webView.uiDelegate = self
    }

    func setupBarButtons() {
        navigationItem.rightBarButtonItem = .close()
        navigationItem.rightBarButtonItem?.tap = closeAction

        let webView = self.webView
        let spacer = UIBarButtonItem(.flexibleSpace, target: nil, action: nil)
        let fixedSpacer = UIBarButtonItem(.fixedSpace, target: nil, action: nil)
        fixedSpacer.width = Constant.navSpacerWidth
        back = UIBarButtonItem(UIImage(named: "LeftArrow"), onTap: { webView.goBack() })
        forward = UIBarButtonItem(UIImage(named: "RightArrow"), onTap: { webView.goForward() })
        refresh = UIBarButtonItem("↻", onTap: { webView.reload() })

        let buttons = [back, fixedSpacer, forward, spacer, refresh].compactMap { $0 }
        setToolbarItems(buttons, animated: false)
    }

    func updateNavigationButtons() {
        back?.isEnabled = webView.canGoBack
        forward?.isEnabled = webView.canGoForward
    }
}

// MARK: - Mail handling

extension WebViewController: MFMailComposeViewControllerDelegate, MFMessageComposeViewControllerDelegate {

    func presentMailCompose(emailAddress: String, subject: String? = nil, body: String? = nil) {
        guard MFMailComposeViewController.canSendMail() else {
            return
        }
        
        let emailVc = MFMailComposeViewController()
        emailVc.setToRecipients([emailAddress.replacingOccurrences(of: "%40", with: "@")])
        emailVc.setSubject(subject ?? "")

        if let body = body {
            emailVc.setMessageBody(body, isHTML: false)
        }

        emailVc.mailComposeDelegate = self
        present(emailVc, animated: true)
    }

    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        dismiss()
    }

    func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        dismiss()
    }
}

// MARK: - Constant

private extension WebViewController {

    enum Constant {
        static let navSpacerWidth: CGFloat = 30
    }
}
