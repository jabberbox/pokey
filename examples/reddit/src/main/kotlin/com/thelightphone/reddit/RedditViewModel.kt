package com.thelightphone.reddit

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val BRAVE_KEY = "BSA3aB3BqQLLqJZy4Ry-CTomG84mlF3"

sealed class RedditMode {
    data object Search : RedditMode()
    data object Loading : RedditMode()
    data class Results(val query: String, val posts: List<RedditPost>) : RedditMode()
    data class Post(
        val post: RedditPost,
        val comments: List<RedditComment>,
        val loadingComments: Boolean = false,
    ) : RedditMode()
}

data class RedditUiState(
    val mode: RedditMode = RedditMode.Search,
    val query: String = "",
    val error: String? = null,
)

class RedditViewModel : LightViewModel<Unit>() {
    private val api = RedditApi()
    private val _uiState = MutableStateFlow(RedditUiState())
    val uiState: StateFlow<RedditUiState> = _uiState.asStateFlow()

    private var lastResults: RedditMode.Results? = null

    fun searchWithQuery(query: String) {
        val key = BRAVE_KEY
        _uiState.update { it.copy(query = query, mode = RedditMode.Loading, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            api.search(query, key).fold(
                onSuccess = { posts ->
                    val results = RedditMode.Results(query, posts)
                    lastResults = results
                    _uiState.update { it.copy(mode = results) }
                },
                onFailure = { err ->
                    android.util.Log.e("RedditSearch", "search failed", err)
                    _uiState.update { it.copy(mode = RedditMode.Search, error = "Search failed. Check your connection.") }
                },
            )
        }
    }

    fun openPost(post: RedditPost) {
        _uiState.update { it.copy(mode = RedditMode.Post(post, emptyList(), loadingComments = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            api.comments(post.id).fold(
                onSuccess = { comments ->
                    _uiState.update { state ->
                        val current = state.mode as? RedditMode.Post ?: return@update state
                        state.copy(mode = current.copy(comments = comments, loadingComments = false))
                    }
                },
                onFailure = {
                    _uiState.update { state ->
                        val current = state.mode as? RedditMode.Post ?: return@update state
                        state.copy(mode = current.copy(loadingComments = false))
                    }
                },
            )
        }
    }

    fun goBack() {
        _uiState.update { state ->
            when (state.mode) {
                is RedditMode.Post -> state.copy(mode = lastResults ?: RedditMode.Search, error = null)
                is RedditMode.Results -> state.copy(mode = RedditMode.Search, query = "", error = null)
                else -> state
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        api.close()
    }
}
