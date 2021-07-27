//
//  AboutViewController.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-04-05.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import SafariServices

class AboutViewController: UIViewController {

    private let titleLabel = UILabel(font: .customBold(size: 26.0), color: .white)
    private let logo = UIImageView(image: #imageLiteral(resourceName: "LogoCutout").withRenderingMode(.alwaysTemplate))
    private let logoBackground = MotionGradientView()
    private let walletID = WalletIDCell()
    private let blog = AboutCell(text: S.About.blog)
    private let twitter = AboutCell(text: S.About.twitter)
    private let reddit = AboutCell(text: S.About.reddit)
    private let privacy = UIButton(type: .system)
    private let footer = UILabel.wrapping(font: .customBody(size: 13.0), color: Theme.primaryText)

    private lazy var scrollView = UIScrollView()
    private lazy var logoContainer = UIView()
    private lazy var stack = VStackView([
        titleLabel, logoContainer, walletID, blog, twitter, reddit, privacy, footer
    ])

    private weak var keyStore: KeyStore?

    init(keyStore: KeyStore?) {
        super.init(nibName: nil, bundle: nil)
        self.keyStore = keyStore
    }

    override func viewDidLoad() {
        addSubviews()
        addConstraints()
        setData()
        setActions()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        fatalError("Not supported")
    }

    private func addSubviews() {
        view.addSubview(scrollView)
        scrollView.addSubview(stack)
        logoContainer.addSubview(logoBackground)
        logoBackground.addSubview(logo)
    }

    private func addConstraints() {
        let spacing = C.padding[(E.isIPhone6OrSmaller) ? 1 : 2]
        let insets = UIEdgeInsets(forConstrains: spacing)
        scrollView.constrain(toSuperviewEdges: nil)
        stack.constrain(toSuperviewEdges: insets)
        stack.constrain([
            stack.widthAnchor.constraint(equalTo: view.widthAnchor, constant: -spacing * 2)
        ])
        stack.spacing = spacing
        logo.constrain(toSuperviewEdges: nil)
        logoBackground.constrain([
            logoBackground.centerXAnchor.constraint(equalTo: logoContainer.centerXAnchor),
            logoBackground.topAnchor.constraint(equalTo: logoContainer.topAnchor),
            logoBackground.bottomAnchor.constraint(equalTo: logoContainer.bottomAnchor),
            logoBackground.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.5),
            logoBackground.heightAnchor.constraint(equalTo: logoBackground.widthAnchor, multiplier: logo.image!.size.height/logo.image!.size.width)
        ])
    }

    private func setData() {
        view.backgroundColor = .darkBackground
        logo.tintColor = .darkBackground
        titleLabel.text = S.About.title
        privacy.setTitle(S.About.privacy, for: .normal)
        privacy.titleLabel?.font = UIFont.customBody(size: 13.0)
        privacy.tintColor = .primaryButton
        footer.textAlignment = .center
        if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String, let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
            let hydra = UserDefaults.cosmos.hydraActivated ? "\nHydra" : ""
            var text = String(format: S.About.footer, version, build) + hydra

            if let keyStore = keyStore, E.isDebug || E.isTestFlight || E.isTestnet {
                let authProvider = IosBrdAuthProvider(walletAuthenticator: keyStore)
                let pubKey = "\n\nPublic key:\n" + authProvider.publicKey()
                let token = "\n\nToken:\n" + (authProvider.token ?? "")
                let deviceId = "\n\nDevice Id:\n" + authProvider.deviceId()
                let walletId = "\n\nWallet Id:\n" + (authProvider.walletId() ?? "")
                let pushToken = "\n\nPush token:\n" + "\(UserDefaults.pushToken)"
                text += pubKey + token + deviceId + walletId + pushToken
            }

            footer.text =  text
        }
    }

    private func setActions() {
        blog.button.tap = strongify(self) { myself in
            myself.presentURL(string: "https://brd.com/blog/")
        }
        twitter.button.tap = strongify(self) { myself in
            myself.presentURL(string: "https://twitter.com/brdhq")
        }
        reddit.button.tap = strongify(self) { myself in
            myself.presentURL(string: "https://reddit.com/r/brdapp/")
        }
        privacy.tap = strongify(self) { myself in
            myself.presentURL(string: "https://brd.com/privacy")
        }

        if let keyStore = keyStore, E.isDebug || E.isTestFlight || E.isTestnet {
            footer.isUserInteractionEnabled = true
            footer.addGestureRecognizer(
                UITapGestureRecognizer(target: self, action: #selector(footerAction(_:)))
            )
        }
    }

    @objc private func footerAction(_ sender: Any?) {
        UIPasteboard.general.string = footer.text
    }

    private func presentURL(string: String) {
        let vc = SFSafariViewController(url: URL(string: string)!)
        self.present(vc, animated: true, completion: nil)
    }
}
