package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoveMediaResponse (
    val result: Boolean,
    val id: Int,
    val message: String = ""
)