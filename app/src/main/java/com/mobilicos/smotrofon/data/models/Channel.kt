package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Channel (
    val id: Int,
    val title: String,
    val text: String,
    val image: String,
    val date_added: String,
    val videos_count: Int,
    val audios_count: Int
)