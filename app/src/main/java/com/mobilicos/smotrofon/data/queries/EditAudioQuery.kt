package com.mobilicos.smotrofon.data.queries

import com.mobilicos.smotrofon.Config
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditAudioQuery(
    val key: String,
    val id: Int,
    val title: String = "",
    var text: String = "",
    var image: String = ""
)