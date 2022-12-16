package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Step(
    val id: Int,
    @Json(name = "step_number") val stepNumber: Int,
    val title: String,
    val text: String,
    @Json(name = "sort_order") val sortOrder: Int,
    @Json(name = "frames_count") val framesCount: Int,
    val frames: List<Frame>
)
