# Cosmos Api Client

A [Ktor](https://ktor.io) powered wrapper for the new [BRD-Web](https://gitlab.com/breadwallet/brd-web) implementation.

## Usage

### Kotlin
```kotlin
val client = BRDApiClient.create(
    authProvider = AndroidBRDAuthProvider(direct.instance()),
    host = "https://api2.breadwallet.com" // Optional, Default: brd-web-staging.herokuapp.com
)

val response = client.getCurrencies()

when (response) {
    is BrdCurrencyResponse.Success -> {
        println(response.currencies.first())
        // Output: BrdCurrency(code=eth, name=Ethereum, ...)
    }
    is BrdCurrencyResponse.Error -> {
        error("Failed to get currencies (${response.status}): ${response.body}")
    }
}
```


### Swift
```swift
let authProvider = IosBRDAuthProvider(walletAuthenticator: authenticator)
let client = BRDApiClientCompanion.init().create(authProvider: authProvider)

client.getCurrencies(mainnet: true) { (response, internalError) in
    switch response {
    case let response as BrdCurrencyResponse.Success:
        print("\(response.currencies.first!.description())")
        // Output: BrdCurrency(code=eth, name=Ethereum, ...)
    case let response as BrdCurrencyResponse.Error:
        print("Failed to get currencies \(response.status): \(response.body)")
    default:
        print("Internal error \(String(describing: internalError))")
    }
}
```
