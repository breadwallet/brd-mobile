package brd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.net.URL

private const val ZENDESK_DOWNLOAD_URL = "https://bwsupport.zendesk.com/api/v2/help_center/en-us"
private const val ASSETS_PATH = "src/%s/assets"
private const val IOS_PATH = "/brd-ios/breadwallet/Resources/support/"

val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
}

open class DownloadSupportArticles :  DefaultTask() {

    @TaskAction
    fun run() {
        // Zendesk Support articles
        downloadZenDeskSupportArticles(
            "$ZENDESK_DOWNLOAD_URL/categories/360000438414/articles.json?per_page=100",
            "secondary_articles.json"
        )
        downloadZenDeskSupportArticles(
            "$ZENDESK_DOWNLOAD_URL/categories/360000437273/articles.json?per_page=100",
            "main_articles.json"
        )
        downloadZenDeskSupportArticles(
            "$ZENDESK_DOWNLOAD_URL/categories/360000437273/sections.json?per_page=200",
            "sections.json",
            false
        )
    }

    /** Download contents of [URL], remove un-needed data, writes [toFiles] */
    private fun downloadZenDeskSupportArticles(
        url: String,
        fileName: String,
        isArticle: Boolean = true,
        sourceFolder: String = "main"
    ) {
        val supportFolder =
            File(project.projectDir, "${ASSETS_PATH.format(sourceFolder)}/support").apply {
                if (!exists()) check(mkdirs()) {
                    "Failed to create zendesk support directory: $absolutePath"
                }
            }

        var components = project.projectDir.parent.split("/").toMutableList()
        components.removeLast()

        URL(url).openStream().use { input ->
            val data = input.bufferedReader().use(BufferedReader::readText)
            val output = if (isArticle) {
                val container = json.decodeFromString<ArticlesContainer>(data)
                json.encodeToString(container.articles)
            } else {
                val container = json.decodeFromString<SectionsContainer>(data)
                json.encodeToString(container.sections)
            }
            listOf(
                File(supportFolder, fileName),
                File(components.joinToString("/") + IOS_PATH + fileName)
            ).forEach {
                it.writeText(output)
            }
        }
    }

    @Serializable
    data class SectionsContainer(
        val sections: List<Section>
    )

    @Serializable
    data class ArticlesContainer(
        val articles: List<Article>
    )

    @Serializable
    data class Section(
        val id: Long,
        @SerialName("name")
        val title: String,
        val position: Int,
    )

    @Serializable
    data class Article(
        val id: Long,
        val title: String,
        @SerialName("section_id")
        val sectionId: Long,
        val promoted: Boolean,
        val position: Int,
        @SerialName("label_names")
        val labelNames: List<String> = emptyList(),
        val body: String,
    )
}
