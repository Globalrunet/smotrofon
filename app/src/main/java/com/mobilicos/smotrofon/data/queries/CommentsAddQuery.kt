package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentsAddQuery(
    val app_label: String = "",
    val model: String = "",
    val object_id: Int = 0,
    val parent_id: Int = 0,
    val key: String = "",
    val text: String = "",
    val images: List<String>? = null
)