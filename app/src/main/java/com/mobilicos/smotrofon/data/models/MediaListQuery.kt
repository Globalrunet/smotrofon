package com.mobilicos.smotrofon.data.models

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaListQuery(
    val query: String = "",
    val key: String = "",
    val type: Int = Config.TYPE_VIDEO,
    val sortOrder: Int = 0,
    var version: Int = 0
)