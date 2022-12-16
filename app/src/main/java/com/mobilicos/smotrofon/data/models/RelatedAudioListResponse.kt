package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RelatedAudioListResponse(
    val elements: List<Audio>,
    val totalPages: Int,
    val totalElements: Int
)