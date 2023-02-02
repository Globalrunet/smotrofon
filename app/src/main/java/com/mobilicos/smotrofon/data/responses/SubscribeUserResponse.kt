package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscribeUserResponse (
    val result: Boolean = false,
    val subscribed: Boolean = false,
    val user_id: Int = 0,
    val other_user_id: Int = 0,
    val message: String = "",
    val subscribed_str: String = ""
)