package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentRemoveResponse (
    val result: Boolean = false,
    val message: String = ""
)