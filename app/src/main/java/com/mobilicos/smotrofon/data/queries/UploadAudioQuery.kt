package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadAudioQuery (
    val id: Int = 0,
    val key: String,
    val path: String,
    val next_chunk: Long
)