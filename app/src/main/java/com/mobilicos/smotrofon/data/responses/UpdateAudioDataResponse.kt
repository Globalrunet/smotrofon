package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateAudioDataResponse(
    val result: Boolean,
    val id: Int,
    val title: String,
    val text: String = "",
    val image: String = ""
)