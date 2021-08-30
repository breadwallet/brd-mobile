/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/21/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd

private val ciTag = System.getenv("CI_COMMIT_TAG")
    ?.split("-") // <target>-x.x.x.x
    ?.lastOrNull()
    ?.split(".")
    ?.map(String::toInt)

object BrdRelease {
    /** Major version. Usually affected by marketing. Maximum value: 99 */
    private val marketing = ciTag?.firstOrNull() ?: 4

    /** Minor version. Usually affected by product. Maximum value: 99 */
    private val product = ciTag?.get(1) ?: 12

    /** Hot fix version. Usually affected by engineering. Maximum value: 9 */
    private val engineering = ciTag?.get(2) ?: 0

    /** Build version. Increase for each new build. Maximum value: 999 */
    private val build = ciTag?.lastOrNull() ?: 5

    init {
        check(marketing in 0..99)
        check(product in 0..99)
        check(engineering in 0..9)
        check(build in 0..999)
    }

    // The version code must be monotonically increasing. It is used by Android to maintain upgrade/downgrade
    // relationship between builds with a max value of 2 100 000 000.
    val versionCode = (marketing * 1000000) + (product * 10000) + (engineering * 1000) + build
    val versionName = "$marketing.$product.$engineering"
    val buildVersion = build
    val internalVersionName = "$marketing.$product.$engineering.$build"

    const val ANDROID_TARGET_SDK = 29
    const val ANDROID_COMPILE_SDK = 30
    const val ANDROID_MINIMUM_SDK = 23
    const val ANDROID_BUILD_TOOLS = "30.0.2"
}
