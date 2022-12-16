package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaDataClass(
    @Json(name = "name")
    val name: String = "PET",
    @Json(name = "nation")
    val nation: String = "RUSSIAN",
    @Json(name = "age")
    val age: Int,
    @Json(name = "color")
    val color: Int
)
