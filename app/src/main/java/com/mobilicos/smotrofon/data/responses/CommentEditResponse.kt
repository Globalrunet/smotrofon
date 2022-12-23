package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentEditResponse (
    val result: Boolean = false,
    val comment_id: Int = 0,
    val text: String = ""
)