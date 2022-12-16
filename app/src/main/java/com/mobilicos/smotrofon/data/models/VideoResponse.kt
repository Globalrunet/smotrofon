package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoResponse(
    val elements: List<Media>,
    val totalPages: Int,
    val totalElements: Int
)