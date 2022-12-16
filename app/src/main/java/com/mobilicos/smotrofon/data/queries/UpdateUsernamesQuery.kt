package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUsernamesQuery (
    val key: String,
    val firstName: String,
    val lastName: String
)