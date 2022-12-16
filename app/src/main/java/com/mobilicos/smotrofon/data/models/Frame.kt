package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Frame(
    val id: Int,
    @Json(name = "sort_order") val sortOrder: Int,
    val image: String
)
