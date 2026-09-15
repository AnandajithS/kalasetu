package com.example.kalasetu.repository

import com.example.kalasetu.GetPostsQuery
import com.example.kalasetu.data.ApiClient
import com.apollographql.apollo.api.Optional

class FeedRepository {

    private val apolloClient = ApiClient.apolloClient

    suspend fun getPosts(
        limit: Int = 20,
        offset: Int = 0
    ) =
        apolloClient
            .query(
                GetPostsQuery(
                    limit = Optional.Present(limit),
                    offset = Optional.Present(offset)
                )
            )
            .execute()
}