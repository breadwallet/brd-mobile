/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd

fun getChangelog(): String {
    val cmdGetCurrentTag = "git describe --tags --abbrev=-0"
    var currentTag = System.getenv("CI_COMMIT_TAG")
    var previousTag = cmdGetCurrentTag.eval()

    if (currentTag == null || currentTag == "") {
        currentTag = "HEAD"
    } else if (currentTag == previousTag) {
        val cmdGetPreviousTagRevision = "git rev-list --tags --skip=1 --max-count=1"
        val previousTagRevision = cmdGetPreviousTagRevision.eval()
        val cmdGetPreviousTag = "git describe --abbrev=0 --tags $previousTagRevision"
        previousTag = cmdGetPreviousTag.eval()
    }
    return "git log $previousTag..$currentTag --no-merges --pretty=format:%s".eval()
}