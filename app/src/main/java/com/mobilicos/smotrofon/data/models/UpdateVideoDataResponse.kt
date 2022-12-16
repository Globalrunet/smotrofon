package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateVideoDataResponse(
    val result: Boolean,
    val id: Int,
    val title: String,
    val text: String = "",
    val image: String = ""
)