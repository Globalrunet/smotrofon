package com.mobilicos.smotrofon.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Comment (
    val app_label: String,
    val model: String,
    val object_id: Int,
    val total_elements: Int = 0,
    val id: Int,
    val parent_id: Int,
    val text: String,
    val date_added: String,
    val likes_count: Int,
    val replies_counter: Int,
    val user_id: Int,
    val user_full_name: String,
    val user_icon: String,
    val img: String = ""
)