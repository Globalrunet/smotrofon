package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserPasswordResponse(
    val result: Boolean,
    val message: String = ""
)