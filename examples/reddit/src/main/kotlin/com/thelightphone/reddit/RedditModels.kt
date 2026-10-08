package com.thelightphone.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ArcticShiftPostsResponse(val data: List<ArcticShiftPost>)

@Serializable
internal data class ArcticShiftPost(
    val id: String = "",
    val title: String = "",
    val selftext: String = "",
    val score: Int = 0,
    val subreddit: String = "",
    @SerialName("num_comments") val numComments: Int = 0,
    val permalink: String = "",
    val author: String = "",
    val created: Long = 0L,
)

@Serializable
internal data class ArcticShiftCommentsResponse(val data: List<ArcticShiftComment>)

@Serializable
internal data class ArcticShiftComment(
    val id: String = "",
    @SerialName("parent_id") val parentId: String = "",
    val author: String = "",
    val body: String = "",
    val score: Int = 0,
    val created: Long = 0L,
)

@Serializable
internal data class BraveSearchResponse(val web: BraveWebResults? = null)

@Serializable
internal data class BraveWebResults(val results: List<BraveResult> = emptyList())

@Serializable
internal data class BraveResult(val url: String = "", val title: String = "")

data class RedditPost(
    val id: String,
    val title: String,
    val selftext: String,
    val score: Int,
    val subreddit: String,
    val numComments: Int,
    val permalink: String,
    val created: Long,
)

data class RedditComment(
    val id: String,
    val parentId: String,
    val author: String,
    val body: String,
    val score: Int,
    val created: Long,
)
