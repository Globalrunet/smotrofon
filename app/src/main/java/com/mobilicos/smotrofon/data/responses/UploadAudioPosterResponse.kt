package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadAudioPosterResponse(
    val result: Boolean,
    val id: Int,
    val image: String = ""
)