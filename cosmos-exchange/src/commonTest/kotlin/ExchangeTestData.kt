/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

const val UK_JSON = """
    {
        "code": "gb",
        "name": "United Kingdom",
        "currency": {
            "code": "gbp",
            "name": "British Pound",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:gbp"
        },
        "regions": []
    }
"""

const val USA_JSON = """
    {
        "code": "us",
        "name": "United States of America",
        "currency": {
            "code": "usd",
            "name": "US Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:usd"
        },
        "regions": [
            {
                "code": "al",
                "name": "Alabama"
            },
            {
                "code": "ak",
                "name": "Alaska"
            },
            {
                "code": "as",
                "name": "American Samoa"
            },
            {
                "code": "az",
                "name": "Arizona"
            },
            {
                "code": "ar",
                "name": "Arkansas"
            },
            {
                "code": "ca",
                "name": "California"
            },
            {
                "code": "co",
                "name": "Colorado"
            },
            {
                "code": "ct",
                "name": "Connecticut"
            },
            {
                "code": "de",
                "name": "Delaware"
            },
            {
                "code": "dc",
                "name": "District Of Columbia"
            },
            {
                "code": "fm",
                "name": "Federated States Of Micronesia"
            },
            {
                "code": "fl",
                "name": "Florida"
            },
            {
                "code": "ga",
                "name": "Georgia"
            },
            {
                "code": "gu",
                "name": "Guam"
            },
            {
                "code": "hi",
                "name": "Hawaii"
            },
            {
                "code": "id",
                "name": "Idaho"
            },
            {
                "code": "il",
                "name": "Illinois"
            },
            {
                "code": "in",
                "name": "Indiana"
            },
            {
                "code": "ia",
                "name": "Iowa"
            },
            {
                "code": "ks",
                "name": "Kansas"
            },
            {
                "code": "ky",
                "name": "Kentucky"
            },
            {
                "code": "la",
                "name": "Louisiana"
            },
            {
                "code": "me",
                "name": "Maine"
            },
            {
                "code": "mh",
                "name": "Marshall Islands"
            },
            {
                "code": "md",
                "name": "Maryland"
            },
            {
                "code": "ma",
                "name": "Massachusetts"
            },
            {
                "code": "mi",
                "name": "Michigan"
            },
            {
                "code": "mn",
                "name": "Minnesota"
            },
            {
                "code": "ms",
                "name": "Mississippi"
            },
            {
                "code": "mo",
                "name": "Missouri"
            },
            {
                "code": "mt",
                "name": "Montana"
            },
            {
                "code": "ne",
                "name": "Nebraska"
            },
            {
                "code": "nv",
                "name": "Nevada"
            },
            {
                "code": "nh",
                "name": "New Hampshire"
            },
            {
                "code": "nj",
                "name": "New Jersey"
            },
            {
                "code": "nm",
                "name": "New Mexico"
            },
            {
                "code": "ny",
                "name": "New York"
            },
            {
                "code": "nc",
                "name": "North Carolina"
            },
            {
                "code": "nd",
                "name": "North Dakota"
            },
            {
                "code": "mp",
                "name": "Northern Mariana Islands"
            },
            {
                "code": "oh",
                "name": "Ohio"
            },
            {
                "code": "ok",
                "name": "Oklahoma"
            },
            {
                "code": "or",
                "name": "Oregon"
            },
            {
                "code": "pw",
                "name": "Palau"
            },
            {
                "code": "pa",
                "name": "Pennsylvania"
            },
            {
                "code": "pr",
                "name": "Puerto Rico"
            },
            {
                "code": "ri",
                "name": "Rhode Island"
            },
            {
                "code": "sc",
                "name": "South Carolina"
            },
            {
                "code": "sd",
                "name": "South Dakota"
            },
            {
                "code": "tn",
                "name": "Tennessee"
            },
            {
                "code": "tx",
                "name": "Texas"
            },
            {
                "code": "ut",
                "name": "Utah"
            },
            {
                "code": "vt",
                "name": "Vermont"
            },
            {
                "code": "vi",
                "name": "Virgin Islands"
            },
            {
                "code": "va",
                "name": "Virginia"
            },
            {
                "code": "wa",
                "name": "Washington"
            },
            {
                "code": "wv",
                "name": "West Virginia"
            },
            {
                "code": "wi",
                "name": "Wisconsin"
            },
            {
                "code": "wy",
                "name": "Wyoming"
            }
        ]
    }
"""

const val CURRENCIES_JSON = """
    {
        "ant": {
            "code": "ant",
            "name": "Aragon V2",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xa117000000f279d81a1d3cc75430faa017fa5a2e"
        },
        "bat": {
            "code": "bat",
            "name": "Basic Attention",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x0D8775F648430679A709E98d2b0Cb6250d2887EF"
        },
        "bch": {
            "code": "bch",
            "name": "Bitcoin Cash",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "bitcoincash-mainnet:__native__"
        },
        "bnt": {
            "code": "bnt",
            "name": "Bancor Network Token",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x1F573D6Fb3F13d689FF844B4cE37794d79a7FF1C"
        },
        "brd": {
            "code": "brd",
            "name": "Bread",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x558ec3152e2eb2174905cd19aea4e34a23de9ad6"
        },
        "btc": {
            "code": "btc",
            "name": "Bitcoin",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "bitcoin-mainnet:__native__"
        },
        "busd": {
            "code": "busd",
            "name": "Binance USD",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x4fabb145d64652a948d72533023f6e7a623c7c53"
        },
        "cvc": {
            "code": "cvc",
            "name": "Civic",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x41e5560054824ea6b0732e656e3ad64e20e94e45"
        },
        "dai": {
            "code": "dai",
            "name": "Dai",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x6b175474e89094c44da98b954eedeac495271d0f"
        },
        "dent": {
            "code": "dent",
            "name": "Dent",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x3597bfd533a99c9aa083587b074434e61eb0a258"
        },
        "dnt": {
            "code": "dnt",
            "name": "district0x",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x0abdace70d3790235af448c88547603b945604ea"
        },
        "enj": {
            "code": "enj",
            "name": "Enjin",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xF629cBd94d3791C9250152BD8dfBDF380E2a3B9c"
        },
        "eth": {
            "code": "eth",
            "name": "Ethereum",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:__native__"
        },
        "fun": {
            "code": "fun",
            "name": "FunFair",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x419D0d8BdD9aF5e606Ae2232ed285Aff190E711b"
        },
        "gno": {
            "code": "gno",
            "name": "Gnosis",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x6810e776880C02933D47DB1b9fc05908e5386b96"
        },
        "gusd": {
            "code": "gusd",
            "name": "Gemini Dollar",
            "decimals": 2,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x056fd409e1d7a124bd7017459dfea2f387b6d5cd"
        },
        "hbar": {
            "code": "hbar",
            "name": "Hedera Hashgraph",
            "decimals": 9,
            "type": "crypto",
            "currency_id": "hedera-mainnet:__native__"
        },
        "knc": {
            "code": "knc",
            "name": "Kyber Network",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xdd974D5C2e2928deA5F71b9825b8b646686BD200"
        },
        "link": {
            "code": "link",
            "name": "ChainLink",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x514910771af9ca656af840dff83e8264ecf986ca"
        },
        "lrc": {
            "code": "lrc",
            "name": "Loopring",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xbbbbca6a901c926f240b89eacb641d8aec7aeafd"
        },
        "mana": {
            "code": "mana",
            "name": "Decentraland",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x0F5D2fB29fb7d3CFeE444a200298f468908cC942"
        },
        "mkr": {
            "code": "mkr",
            "name": "Maker",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2"
        },
        "nexo": {
            "code": "nexo",
            "name": "NEXO",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xb62132e35a6c13ee1ee0f84dc5d40bad8d815206"
        },
        "nmr": {
            "code": "nmr",
            "name": "Numeraire",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x1776e1F26f98b1A5dF9cD347953a26dd3Cb46671"
        },
        "omg": {
            "code": "omg",
            "name": "OmiseGo",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xd26114cd6EE289AccF82350c8d8487fedB8A0C07"
        },
        "pax": {
            "code": "pax",
            "name": "Paxos Standard",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x8e870d67f660d95d5be530380d0ec0bd388289e1"
        },
        "pay": {
            "code": "pay",
            "name": "TenXPay",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xB97048628DB6B661D4C2aA833e95Dbe1A905B280"
        },
        "poly": {
            "code": "poly",
            "name": "Polymath",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x9992eC3cF6A55b00978cdDF2b27BC6882d88D1eC"
        },
        "powr": {
            "code": "powr",
            "name": "Power Ledger",
            "decimals": 6,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x595832f8fc6bf59c85c527fec3740a1b7a361269"
        },
        "ppt": {
            "code": "ppt",
            "name": "Populous",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xd4fa1460F537bb9085d22C7bcCB5DD450Ef28e3a"
        },
        "rcn": {
            "code": "rcn",
            "name": "Ripio Credit Network",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xf970b8e36e23f7fc3fd752eea86f8be8d83375a6"
        },
        "rlc": {
            "code": "rlc",
            "name": "iEx.ec",
            "decimals": 9,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x607F4C5BB672230e8672085532f7e901544a7375"
        },
        "storj": {
            "code": "storj",
            "name": "Storj",
            "decimals": 8,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xB64ef51C888972c908CFacf59B47C1AfBC0Ab8aC"
        },
        "tel": {
            "code": "tel",
            "name": "Telcoin",
            "decimals": 2,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x467bccd9d29f223bce8043b84e8c8b282827790f"
        },
        "tusd": {
            "code": "tusd",
            "name": "TrueUSD",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x0000000000085d4780B73119b644AE5ecd22b376"
        },
        "uni": {
            "code": "uni",
            "name": "Uniswap",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"
        },
        "usdc": {
            "code": "usdc",
            "name": "USD Coin",
            "decimals": 6,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
        },
        "usdt": {
            "code": "usdt",
            "name": "Tether USD",
            "decimals": 6,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xdac17f958d2ee523a2206206994597c13d831ec7"
        },
        "xrp": {
            "code": "xrp",
            "name": "XRP",
            "decimals": 6,
            "type": "crypto",
            "currency_id": "ripple-mainnet:__native__"
        },
        "zrx": {
            "code": "zrx",
            "name": "0x",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0xE41d2489571d322189246DaFA5ebDe1F4699F498"
        },
        "ars": {
            "code": "ars",
            "name": "Argentine Peso",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:ars"
        },
        "aave": {
            "code": "aave",
            "name": "Aave",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"
        },
        "aud": {
            "code": "aud",
            "name": "Australian Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:aud"
        },
        "usd": {
            "code": "usd",
            "name": "US Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:usd"
        },
        "brl": {
            "code": "brl",
            "name": "Brazilian Real",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:brl"
        },
        "cad": {
            "code": "cad",
            "name": "Canadian Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:cad"
        },
        "chf": {
            "code": "chf",
            "name": "Swiss Franc",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:chf"
        },
        "clp": {
            "code": "clp",
            "name": "Chilean Peso",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:clp"
        },
        "cop": {
            "code": "cop",
            "name": "Colombian Peso",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:cop"
        },
        "czk": {
            "code": "czk",
            "name": "Czech Koruna",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:czk"
        },
        "dkk": {
            "code": "dkk",
            "name": "Danish Krone",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:dkk"
        },
        "eur": {
            "code": "eur",
            "name": "Euro",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:eur"
        },
        "gbp": {
            "code": "gbp",
            "name": "British Pound",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:gbp"
        },
        "glm": {
            "code": "glm",
            "name": "Golem",
            "decimals": 18,
            "type": "crypto",
            "currency_id": "ethereum-mainnet:0x7DD9c5Cba05E151C895FDe1CF355C9A1D5DA6429"
        },
        "hkd": {
            "code": "hkd",
            "name": "Hong Kong Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:hkd"
        },
        "ils": {
            "code": "ils",
            "name": "Israeli New Shekel",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:ils"
        },
        "inr": {
            "code": "inr",
            "name": "Indian Rupee",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:inr"
        },
        "isk": {
            "code": "isk",
            "name": "Icelandic Kr\u00F3na",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:isk"
        },
        "jpy": {
            "code": "jpy",
            "name": "Japanese Yen",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:jpy"
        },
        "krw": {
            "code": "krw",
            "name": "South Korean Won",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:krw"
        },
        "mxn": {
            "code": "mxn",
            "name": "Mexican Peso",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:mxn"
        },
        "myr": {
            "code": "myr",
            "name": "Malaysian Ringgit",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:myr"
        },
        "nok": {
            "code": "nok",
            "name": "Norwegian Krone",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:nok"
        },
        "nzd": {
            "code": "nzd",
            "name": "New Zealand Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:nzd"
        },
        "php": {
            "code": "php",
            "name": "Philippine Piso",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:php"
        },
        "pln": {
            "code": "pln",
            "name": "Polish Zloty",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:pln"
        },
        "sek": {
            "code": "sek",
            "name": "Swedish Krona",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:sek"
        },
        "sgd": {
            "code": "sgd",
            "name": "Singapore Dollar",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:sgd"
        },
        "thb": {
            "code": "thb",
            "name": "Thai Baht",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:thb"
        },
        "try": {
            "code": "try",
            "name": "Turkish Lira",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:try"
        },
        "vnd": {
            "code": "vnd",
            "name": "Vietnamese Dong",
            "decimals": 0,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:vnd"
        },
        "xtz": {
            "code": "xtz",
            "name": "Tezos",
            "decimals": 6,
            "type": "crypto",
            "currency_id": "tezos-mainnet:__native__"
        },
        "zar": {
            "code": "zar",
            "name": "South African Rand",
            "decimals": 2,
            "type": "fiat",
            "currency_id": "meatspace-mainnet:zar"
        }
    }
"""
