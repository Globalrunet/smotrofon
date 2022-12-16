package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadUserImageResponse(
    val result: Boolean,
    val message: String = "",
    val image: String = ""
)