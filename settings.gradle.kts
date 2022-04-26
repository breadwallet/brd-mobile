rootProject.name = "brd-mobile"

include(
    "cosmos-core",
    "cosmos-brd-api-client",
    "cosmos-websocket",
    "cosmos-bundled",
    "cosmos-support",
    "cosmos-preferences",
    "cosmos-bakers-api-client"
)

include("cosmos-exchange")

include(
    "brd-android:app",
    "brd-android:app-core",
    "brd-android:theme"
)

include("cosmos-address-resolver")

includeBuild("external/walletkit/WalletKitJava") {
    dependencySubstitution {
        substitute(module("com.blockset.walletkit:WalletKitBRD-JRE"))
            .using(project(":WalletKitBRD-JRE"))
        substitute(module("com.blockset.walletkit:WalletKitBRD-Android"))
            .using(project(":WalletKitBRD-Android"))
    }
}

includeBuild("external/redacted-compiler-plugin") {
    dependencySubstitution {
        substitute(module("dev.zacsweers.redacted:redacted-compiler-gradle-plugin"))
            .with(project(":redacted-compiler-plugin-gradle"))
        substitute(module("dev.zacsweers.redacted:redacted-compiler-plugin-annotations"))
            .with(project(":redacted-compiler-plugin-annotations"))
    }
}
include(":cosmos-feature-promotion")
