package com.mobilicos.smotrofon.data.responses

import com.mobilicos.smotrofon.data.models.Audio
import com.mobilicos.smotrofon.data.models.Media
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AudioResponse(
    val elements: List<Audio>,
    val totalPages: Int,
    val totalElements: Int
)