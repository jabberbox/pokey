package com.thelightphone.reddit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

private const val ARCTIC = "https://arctic-shift.photon-reddit.com/api"
private const val BRAVE = "https://api.search.brave.com/res/v1/web/search"

internal class RedditApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    suspend fun search(query: String, braveApiKey: String): Result<List<RedditPost>> = runCatching {
        val encoded = URLEncoder.encode("site:reddit.com ${query.trim()}", UTF_8.name())
        val braveResponse = client.get("$BRAVE?q=$encoded&count=15&result_filter=web") {
            header("X-Subscription-Token", braveApiKey)
            header("Accept", "application/json")
        }
        if (!braveResponse.status.isSuccess()) {
            throw IllegalStateException("Brave HTTP ${braveResponse.status.value}: ${braveResponse.bodyAsText().take(200)}")
        }
        val brave: BraveSearchResponse = braveResponse.body()
        val ids = brave.web?.results
            .orEmpty()
            .mapNotNull { extractPostId(it.url) }
            .distinct()
            .take(15)
        if (ids.isEmpty()) return@runCatching emptyList()

        val idsParam = ids.joinToString(",") { "t3_$it" }
        val arcticResponse = client.get("$ARCTIC/posts/ids?ids=$idsParam")
        if (!arcticResponse.status.isSuccess()) {
            throw IllegalStateException("Arctic HTTP ${arcticResponse.status.value}: ${arcticResponse.bodyAsText().take(200)}")
        }
        val result: ArcticShiftPostsResponse = arcticResponse.body()
        result.data
            .sortedByDescending { it.created }
            .map { post ->
                RedditPost(
                    id = post.id,
                    title = post.title,
                    selftext = post.selftext,
                    score = post.score,
                    subreddit = post.subreddit,
                    numComments = post.numComments,
                    permalink = post.permalink,
                    created = post.created,
                )
            }
    }

    suspend fun comments(postId: String): Result<List<RedditComment>> = runCatching {
        val url = "$ARCTIC/comments/search?link_id=t3_$postId&limit=50&sort=desc"
        val response = client.get(url)
        if (!response.status.isSuccess()) {
            throw IllegalStateException("HTTP ${response.status.value}: ${response.bodyAsText().take(200)}")
        }
        val result: ArcticShiftCommentsResponse = response.body()
        result.data
            .filter { it.body.isNotBlank() && it.body != "[deleted]" && it.body != "[removed]" }
            .map { RedditComment(id = it.id, parentId = it.parentId, author = it.author, body = it.body, score = it.score, created = it.created) }
    }

    fun close() {
        client.close()
    }
}

private fun extractPostId(url: String): String? {
    return try {
        val path = url.substringAfter("reddit.com").trimEnd('/')
        val parts = path.split("/").filter { it.isNotEmpty() }
        val commentsIdx = parts.indexOf("comments")
        if (commentsIdx >= 0 && commentsIdx + 1 < parts.size) parts[commentsIdx + 1] else null
    } catch (_: Exception) {
        null
    }
}
