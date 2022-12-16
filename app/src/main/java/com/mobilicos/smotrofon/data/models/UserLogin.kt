package com.mobilicos.smotrofon.data.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLogin (
    val result: Boolean,
    val key: String,
    val username: String,
    @Json(name = "first_name") val firstname: String = "",
    @Json(name = "last_name") val lastname: String = "",
    val email: String,
    val description: String,
    val phone: String,
    val site: String,
    val user_icon: String,
    val videos_count: Int,
    val audios_count: Int,
    val user_full_name: String,
    val user_id: Int
)