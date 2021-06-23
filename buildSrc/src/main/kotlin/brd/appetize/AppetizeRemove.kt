/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd.appetize

import brd.eval
import brd.getExtra
import com.android.build.gradle.api.ApplicationVariant
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.extra
import javax.inject.Inject

open class AppetizeRemove @Inject constructor(
    private val buildVariant: ApplicationVariant
) : DefaultTask() {

    private val http = OkHttpClient()

    init {
        group = "Appetize Deployment"
        description = "Remove '${buildVariant.name}' from appetize."
    }

    @TaskAction
    fun delete() {
        val callbackUrl = System.getenv("APPETIZE_CALLBACK")
            ?: project.getExtra("appetizeCallback", "")
        check(callbackUrl.isNotBlank()) {
            "Appetize callback url must be set with APPETIZE_CALLBACK or appetizeCallback."
        }

        val url = callbackUrl.toHttpUrl()
            .newBuilder()
            .addQueryParameter("flavor", buildVariant.flavorName)
            .addQueryParameter("buildType", buildVariant.buildType.name)
            .apply {
                when {
                    project.extra.has("merge") -> {
                        addQueryParameter("type", "merge")
                        addQueryParameter("name", project.extra.get("merge") as String)
                    }
                    project.extra.has("tag") -> {
                        addQueryParameter("type", "tag")
                        addQueryParameter("name", project.extra.get("tag") as String)
                    }
                    else -> {
                        val branch = "git branch --show-current".eval()
                        val user = "git config user.name".eval()
                        addQueryParameter("type", "dev")
                        addQueryParameter("name", if (user.isNotBlank()) {
                            "dev-$user"
                        } else {
                            "dev-$branch"
                        })
                    }
                }
            }
            .build()
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
        http.newCall(request).execute().use { response ->
            check(response.isSuccessful) {
                logger.error(response.body?.string())
                "Appetize callback failed (${response.code})"
            }
        }
    }
}