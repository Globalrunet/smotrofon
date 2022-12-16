package com.mobilicos.smotrofon.data.queries

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadAudioPosterQuery(
    val key: String,
    val id: Int,
    val type: Int,
    var image: String = "",
)