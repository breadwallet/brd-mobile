/**
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 9/3/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

open class PostToSlack : DefaultTask() {

    @Internal
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    @Internal
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json = json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 30000
        }
    }

    @TaskAction
    fun run() {
        runBlocking {
            println("running post to slack")
            postToSlack()
        }
    }

    /** Posts Message to Slack containing Release Build Info */
    private suspend fun postToSlack() {
        try {
            val token = System.getenv("SLACK_TOKEN_RELEASE")
            val version = BrdRelease.internalVersionName
            val url = "https://slack.com/api/files.upload"

            val channels = "#app-releases"
            val comment = ":brd: :android: \nVersion `$version`\n"
            val brdAPkPattern = "brd[A-Za-z]*-(debug|release)-$version.apk$".toRegex()

            val files = File("${project.buildDir.absolutePath}/outputs/apk")
                .walk()
                .toList()
                .filter { it.isFile && it.name.matches(brdAPkPattern) }
                .map { file ->
                    val response: HttpResponse = client.submitFormWithBinaryData(
                        url = url,
                        formData = formData {
                            append("filename", file.name)
                            append("filetype", file.name.substringAfterLast("."))
                            append("file", file.readBytes(), Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    ContentType.Application.FormUrlEncoded
                                )
                                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                            })
                        }
                    ) {
                        headers { append("Authorization", "Bearer $token") }
                    }

                    val slackFileResponse = response.receive<SlackFileResponse>()
                    slackFileResponse.fileDetail.permalink
                }

            client.post<Unit>("https://slack.com/api/chat.postMessage") {
                headers {
                    append("Authorization", "Bearer $token")
                }
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                body = buildJsonObject {
                    put("channel", channels)
                    putJsonArray("attachments") {
                        addJsonObject {
                            put("text", "Release Notes - $version\n\n${getChangelog()}")
                        }
                    }
                    put("text", "$comment \n${files.joinToString("\n")}")
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            println("error")
        }
    }

    @Serializable
    data class SlackFileResponse(
        @SerialName("file") val fileDetail: SlackFile,
        val ok: Boolean
    )

    @Serializable
    data class SlackFile(
        val channels: List<String>,
        val comments_count: Int,
        val created: Int,
        val display_as_bot: Boolean,
        val editable: Boolean,
        val external_type: String,
        val filetype: String,
        val groups: List<String>,
        val has_rich_preview: Boolean,
        val id: String,
        val ims: List<String>,
        val is_external: Boolean,
        val is_public: Boolean,
        val is_starred: Boolean,
        val mimetype: String,
        val mode: String,
        val name: String,
        val permalink: String,
        val permalink_public: String,
        val pretty_type: String,
        val public_url_shared: Boolean,
        val size: Int,
        val timestamp: Int,
        val title: String,
        val url_private: String,
        val url_private_download: String,
        val user: String,
        val username: String
    )
}
