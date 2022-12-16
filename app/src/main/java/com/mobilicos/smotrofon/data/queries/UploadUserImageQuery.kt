package com.mobilicos.smotrofon.data.queries

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadUserImageQuery(
    val key: String,
    var image: String = "",
)