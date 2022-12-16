package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LessonsListResponse(
    val elements: List<Lesson>,
    val totalPages: Int,
    val totalElements: Int
)