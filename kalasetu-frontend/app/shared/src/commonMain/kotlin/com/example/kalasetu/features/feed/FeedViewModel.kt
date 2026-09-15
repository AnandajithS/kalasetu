package com.example.kalasetu.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kalasetu.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()

    private val _posts = MutableStateFlow<List<ArtistPost>>(emptyList())
    val posts: StateFlow<List<ArtistPost>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = repository.getPosts()

                println("========== FEED RESPONSE ==========")
                println("data = ${response.data}")
                println("errors = ${response.errors}")
                println("exception = ${response.exception}")

                if (!response.errors.isNullOrEmpty()) {
                    println("FEED FAILED: ${response.errors}")
                    return@launch
                }

                val backendPosts = response.data?.posts.orEmpty()

                _posts.value = backendPosts.map { post ->
                    ArtistPost(
                        id = post.id.toInt(),
                        artistName = post.userName,
                        craft = post.categoryName ?: "",
                        location = "",
                        timeAgo = post.createdAt,
                        avatarUrl = "",
                        images = post.media
                            .sortedBy { it.sortOrder }
                            .map { it.url },
                        caption = post.content,
                        likes = post.likeCount,
                        comments = post.commentCount
                    )
                }

                println("Posts loaded: ${_posts.value.size}")

            } catch (e: Exception) {
                println("FEED ERROR: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}