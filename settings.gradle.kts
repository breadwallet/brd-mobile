rootProject.name = "brd-mobile"

include(
    "cosmos-core",
    "cosmos-brd-api-client",
    //"cosmos-websocket",
    "cosmos-bundled",
    "cosmos-preferences",
    "cosmos-bakers-api-client"
)

include(
    "brd-android:app",
    "brd-android:app-core",
    "brd-android:theme"
)
