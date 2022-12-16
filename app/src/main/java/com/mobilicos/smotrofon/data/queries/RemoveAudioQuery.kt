package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoveAudioQuery(
    val key: String,
    val id: Int,
    val type: Int
)