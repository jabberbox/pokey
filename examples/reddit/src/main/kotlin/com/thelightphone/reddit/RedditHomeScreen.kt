package com.thelightphone.reddit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CommentNode(val comment: RedditComment, val depth: Int)

private fun buildCommentTree(comments: List<RedditComment>): List<CommentNode> {
    val childrenOf = mutableMapOf<String, MutableList<RedditComment>>()
    val topLevel = mutableListOf<RedditComment>()
    for (c in comments) {
        if (c.parentId.startsWith("t3_")) {
            topLevel.add(c)
        } else {
            val pid = c.parentId.removePrefix("t1_")
            childrenOf.getOrPut(pid) { mutableListOf() }.add(c)
        }
    }
    val result = mutableListOf<CommentNode>()
    fun traverse(c: RedditComment, depth: Int) {
        result.add(CommentNode(c, depth))
        childrenOf[c.id]?.sortedByDescending { it.score }?.forEach { traverse(it, depth + 1) }
    }
    topLevel.sortedByDescending { it.score }.forEach { traverse(it, 0) }
    return result
}

private fun formatAge(createdUtc: Long): String {
    if (createdUtc == 0L) return ""
    val nowSec = System.currentTimeMillis() / 1000
    val ageSec = nowSec - createdUtc
    return when {
        ageSec < 3600 -> "${ageSec / 60}m ago"
        ageSec < 86400 -> "${ageSec / 3600}h ago"
        ageSec < 7 * 86400 -> "${ageSec / 86400}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(createdUtc * 1000))
    }
}

@InitialScreen
class RedditHomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, RedditViewModel>(sealedActivity) {

    override val viewModelClass: Class<RedditViewModel>
        get() = RedditViewModel::class.java

    override fun createViewModel() = RedditViewModel()

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.uiState.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                when (val mode = state.mode) {
                    is RedditMode.Search -> SearchContent(state)
                    is RedditMode.Loading -> LoadingContent()
                    is RedditMode.Results -> ResultsContent(mode)
                    is RedditMode.Post -> PostContent(mode)
                }
            }
        }
    }

    @Composable
    private fun SearchContent(state: RedditUiState) {
        LightTopBar(
            leftButton = null,
            center = LightTopBarCenter.Text("reddit"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1f.gridUnitsAsDp()),
        ) {
            LightTextField(
                label = "Search",
                value = state.query,
                placeholder = "search here..",
                onClick = {
                    navigateTo(
                        screenFactory = { RedditEditorScreen(it, "Search", "") },
                        resultCallback = { result ->
                            if (result != null) viewModel.searchWithQuery(result)
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.error != null) {
                LightText(
                    text = state.error,
                    variant = LightTextVariant.Fine,
                    modifier = Modifier.padding(top = 0.75f.gridUnitsAsDp()),
                )
            }
        }
    }

    @Composable
    private fun LoadingContent() {
        LightTopBar(
            leftButton = null,
            center = LightTopBarCenter.Text("reddit"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )
        LightText(
            text = "Loading...",
            variant = LightTextVariant.Copy,
            modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp()),
        )
    }

    @Composable
    private fun ColumnScope.ResultsContent(mode: RedditMode.Results) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(
                icon = LightIcons.BACK,
                onClick = { viewModel.goBack() },
            ),
            center = LightTopBarCenter.Text("Results"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )
        if (mode.posts.isEmpty()) {
            LightText(
                text = "No results.",
                variant = LightTextVariant.Copy,
                modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp()),
            )
        } else {
            LightScrollView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 1f.gridUnitsAsDp()),
            ) {
                mode.posts.forEach { post -> PostRow(post) }
            }
        }
    }

    @Composable
    private fun PostRow(post: RedditPost) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .lightClickable { viewModel.openPost(post) }
                .padding(top = 0.75f.gridUnitsAsDp())
                .padding(end = 1f.gridUnitsAsDp()),
        ) {
            val age = formatAge(post.created)
            val meta = buildString {
                append("r/${post.subreddit}")
                if (age.isNotEmpty()) append(" · $age")
            }
            LightText(text = meta, variant = LightTextVariant.Fine)
            LightText(
                text = post.title,
                variant = LightTextVariant.Copy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 0.2f.gridUnitsAsDp()),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.75f.gridUnitsAsDp())
                    .height(1.dp)
                    .background(LightThemeTokens.colors.contentSecondary.copy(alpha = 0.15f)),
            )
        }
    }

    @Composable
    private fun ColumnScope.PostContent(mode: RedditMode.Post) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(
                icon = LightIcons.BACK,
                onClick = { viewModel.goBack() },
            ),
            center = LightTopBarCenter.Text("r/${mode.post.subreddit}"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 1f.gridUnitsAsDp()),
        ) {
            LightText(
                text = mode.post.title,
                variant = LightTextVariant.Copy,
                modifier = Modifier
                    .padding(bottom = 0.5f.gridUnitsAsDp())
                    .padding(end = 1f.gridUnitsAsDp()),
            )
            val body = mode.post.selftext.trim()
            if (body.isNotEmpty() && body != "[deleted]" && body != "[removed]") {
                LightText(
                    text = body,
                    variant = LightTextVariant.Fine,
                    modifier = Modifier
                        .padding(bottom = 0.75f.gridUnitsAsDp())
                        .padding(end = 1f.gridUnitsAsDp()),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 1f.gridUnitsAsDp(), bottom = 0.75f.gridUnitsAsDp())
                    .height(1.dp)
                    .background(LightThemeTokens.colors.contentSecondary.copy(alpha = 0.3f)),
            )
            if (mode.loadingComments) {
                LightText(
                    text = "Loading comments...",
                    variant = LightTextVariant.Fine,
                    modifier = Modifier.padding(vertical = 0.75f.gridUnitsAsDp()),
                )
            } else if (mode.comments.isEmpty()) {
                LightText(
                    text = "No comments.",
                    variant = LightTextVariant.Fine,
                    modifier = Modifier.padding(vertical = 0.75f.gridUnitsAsDp()),
                )
            } else {
                val nodes = buildCommentTree(mode.comments)
                nodes.forEach { (comment, depth) -> CommentBlock(comment, depth) }
            }
        }
    }

    @Composable
    private fun CommentBlock(comment: RedditComment, depth: Int) {
        val indent = (depth.coerceAtMost(4) * 10).dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indent)
                .height(IntrinsicSize.Min)
                .padding(vertical = 0.5f.gridUnitsAsDp())
                .padding(end = 1f.gridUnitsAsDp()),
        ) {
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(LightThemeTokens.colors.contentSecondary.copy(alpha = 0.3f)),
                )
                Spacer(Modifier.width(6.dp))
            }
            Column(Modifier.weight(1f)) {
                val age = formatAge(comment.created)
                val header = buildString {
                    append(comment.author.uppercase())
                    if (age.isNotEmpty()) append(" · $age")
                }
                LightText(text = header, variant = LightTextVariant.Fine)
                LightText(text = comment.body, variant = LightTextVariant.Copy)
                LightText(
                    text = "${comment.score} pts",
                    variant = LightTextVariant.Fine,
                    modifier = Modifier.padding(top = 0.25f.gridUnitsAsDp()),
                )
            }
        }
    }
}
