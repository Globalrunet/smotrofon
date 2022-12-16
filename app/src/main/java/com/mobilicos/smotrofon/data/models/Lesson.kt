package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Lesson (
    val id: Int,
    val ident: Int,
    val title: String,
    val text: String,
    @Json(name = "show_times") val showTimes: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "user_full_name") val userFullName: String,
    @Json(name = "user_icon") val userIcon: String,
    val image: String,
    @Json(name = "date_added") val dateAdded: String
)
