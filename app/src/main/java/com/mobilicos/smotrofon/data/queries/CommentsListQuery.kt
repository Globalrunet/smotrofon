package com.mobilicos.smotrofon.data.queries

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentsListQuery(
    val app_label: String = "",
    val model: String = "",
    val object_id: Int = 0,
    val key: String = "",
    val sortOrder: Int = 0
)