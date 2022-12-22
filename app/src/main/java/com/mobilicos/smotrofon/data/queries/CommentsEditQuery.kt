package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentsEditQuery(
    val comment_id: Int = 0,
    val key: String = "",
    val text: String = ""
)