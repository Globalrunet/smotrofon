package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoursesStep(
    val id: Int,
    @Json(name = "step_number") val stepNumber: Int,
    val title: String ="",
    val text: String = "",
    val tips: String = "",
    @Json(name = "sort_order") val sortOrder: Int,
    @Json(name = "frames_count") val framesCount: Int,
    @Json(name = "image_extension") val imageExtension: String = "",
    val frames: List<CoursesFrame>
)
