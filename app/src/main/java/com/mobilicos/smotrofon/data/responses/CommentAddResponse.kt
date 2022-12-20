package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentAddResponse (
    val result: Boolean = false,
    val app_label: String = "",
    val model: String = "",
    val object_id: Int = 0,
    val id: Int = 0,
    val parent_id: Int = 0,
    val text: String = "",
    val date_added: String = "",
    val likes_count: Int = 0,
    val replies_counter: Int = 0,
    val user_id: Int = 0,
    val user_full_name: String = "",
    val user_icon: String = ""
)