package com.mobilicos.smotrofon.data.models

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoursesLessonsListQuery(
    val language: String = Config.DEFAULT_LANGUAGE,
    val query: String = "",
    val sortOrder: Int = 0,
    var version: Int = 0
)