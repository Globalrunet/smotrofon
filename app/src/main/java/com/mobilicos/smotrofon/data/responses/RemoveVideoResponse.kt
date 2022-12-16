package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoveVideoResponse (
    val result: Boolean,
    val id: Int,
    val message: String = ""
)