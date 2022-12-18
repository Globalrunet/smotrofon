package com.mobilicos.smotrofon.data.responses

import com.mobilicos.smotrofon.data.models.Audio
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.data.models.Media
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentsListResponse(
    val elements: List<Comment>,
    val totalPages: Int,
    val totalElements: Int
)