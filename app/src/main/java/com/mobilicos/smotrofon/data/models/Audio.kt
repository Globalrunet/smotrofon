package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Audio (
    val id: Int,
    val title: String,
    val text: String,
    val poster: String,
    val poster_large: String,
    val audio_url: String,
    val date_added: String,
    val views_count: Int,
    val comments_count: Int,
    val likes_count: Int,
    val dislikes_count: Int,
    val user_id: Int,
    val user_full_name: String,
    val user_icon: String,
    val user_gender: String
)