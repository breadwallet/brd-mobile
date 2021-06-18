/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd.appetize

import brd.getExtra
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.plugins.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.register
import java.util.Locale

class AppetizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project.pluginManager.hasPlugin("com.android.application")) {
            "AppetizePlugin must be applied after 'com.android.application'."
        }
        val appetizeToken = System.getenv("APPETIZE_TOKEN")
            ?: project.getExtra("appetizeToken", "")
        project.afterEvaluate {
            val app = project.plugins
                .getPlugin(AppPlugin::class)
                .extension as AppExtension
            val uploadTasks = app.applicationVariants.map { variant ->
                project.tasks.register(
                    variant.uploadTaskName,
                    AppetizeUpload::class,
                    variant,
                    appetizeToken
                )
            }
            val removeTasks = app.applicationVariants.map { variant ->
                project.tasks.register(
                    variant.removeTaskName,
                    AppetizeRemove::class,
                    variant
                )
            }
            with(project.tasks) {
                register("appetizeUpload").configure {
                    group = "Appetize Deployment"
                    description = "Upload all variants to appetize."
                    setDependsOn(uploadTasks)
                }
                register("appetizeRemove").configure {
                    group = "Appetize Deployment"
                    description = "Remove all associated variants from appetize."
                    setDependsOn(removeTasks)
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val ApplicationVariant.uploadTaskName: String
        get() = buildString {
            append("appetizeUpload")
            append(flavorName.capitalize(Locale.ROOT))
            append(buildType.name.capitalize(Locale.ROOT))
        }

    @OptIn(ExperimentalStdlibApi::class)
    private val ApplicationVariant.removeTaskName: String
        get() = buildString {
            append("appetizeRemove")
            append(flavorName.capitalize(Locale.ROOT))
            append(buildType.name.capitalize(Locale.ROOT))
        }
}