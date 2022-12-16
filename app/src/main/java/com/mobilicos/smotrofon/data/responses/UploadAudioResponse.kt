package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadAudioResponse (
    val result: Boolean,
    val id: Int = 0,
    val message: String = "",
    val is_end: Boolean = false,
    val size: Long = -1,
    val uploaded_bytes: Long = -1,
    val nextChunk: Long = 0
)