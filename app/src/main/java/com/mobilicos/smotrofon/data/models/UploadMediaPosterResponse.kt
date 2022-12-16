package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadMediaPosterResponse(
    val result: Boolean,
    val id: Int,
    val image: String = ""
)