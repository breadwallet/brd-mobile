#!/bin/zsh

FRAMEWORK_PATH="../cosmos-bundled/build-frameworks/Cosmos.xcframework"
FRAMEWORK_BIN_PATH="$FRAMEWORK_PATH/ios-arm64-simulator/Cosmos.framework/Cosmos"
FRAMEWORK_PATH_PLIST="$FRAMEWORK_PATH/Info.plist"
FRAMEWORK_PATH_ARM_PLIST="$FRAMEWORK_PATH/ios-arm64-simulator/Cosmos.framework/Info.plist"

# Copy arm64 framework and change flags in binary to iphone simulator 

rm -rf "$FRAMEWORK_PATH/ios-x86_64-simulator"
cp -r "$FRAMEWORK_PATH/ios-arm64" "$FRAMEWORK_PATH/ios-arm64-simulator"

# xcrun vtool -arch arm64 -show $FRAMEWORK_BIN_PATH
xcrun vtool -arch arm64 \
              -set-build-version 7 12.0 12.0 \
              -replace \
              -output "$FRAMEWORK_BIN_PATH.reworked" \
              $FRAMEWORK_BIN_PATH

rm $FRAMEWORK_BIN_PATH
mv "$FRAMEWORK_BIN_PATH.reworked" $FRAMEWORK_BIN_PATH

# Update plists

plistBuddy="/usr/libexec/PlistBuddy"

x86Idx="-1"
firstArch=$($plistBuddy -c "Print :AvailableLibraries:0:LibraryIdentifier" "$FRAMEWORK_PATH_PLIST")
secondArch=$($plistBuddy -c "Print :AvailableLibraries:1:LibraryIdentifier" "$FRAMEWORK_PATH_PLIST")

if [[ $firstArch == "ios-x86_64-simulator" ]]; then
	x86Idx="0"
fi

if [[ $secondArch == "ios-x86_64-simulator" ]]; then
	x86Idx="1"
fi

if [[ $x86Idx == "-1" ]]; then
	exit "Could not find ios-x86_64-simulator LibraryIdentifier in $FRAMEWORK_PATH_PLIST"
fi

$plistBuddy -c "Set :CFBundleSupportedPlatforms:0 iPhoneSimulator" "$FRAMEWORK_PATH_ARM_PLIST"
$plistBuddy -c "Set :AvailableLibraries:$x86Idx:SupportedArchitectures:0 arm64" "$FRAMEWORK_PATH_PLIST"
$plistBuddy -c "Set :AvailableLibraries:$x86Idx:LibraryIdentifier ios-arm64-simulator" "$FRAMEWORK_PATH_PLIST"
$plistBuddy -c "Set :AvailableLibraries:$x86Idx:SupportedPlatformVariant simulator" "$FRAMEWORK_PATH_PLIST"

# Sign simulator framework

xcrun codesign --sign - $FRAMEWORK_BIN_PATH
