package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoursesFrame(
    val id: Int = 0,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    val image: String = ""
)
