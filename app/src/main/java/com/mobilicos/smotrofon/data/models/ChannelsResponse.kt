package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChannelsResponse(
    val elements: List<Channel>,
    val totalPages: Int,
    val totalElements: Int
)