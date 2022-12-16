package com.mobilicos.smotrofon.data.queries

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AudioListQuery(
    val query: String = "",
    val key: String = "",
    val type: Int = Config.TYPE_VIDEO,
    val sortOrder: Int = 0,
    var version: Int = 0
)