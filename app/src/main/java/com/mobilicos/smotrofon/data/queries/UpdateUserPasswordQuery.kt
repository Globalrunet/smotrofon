package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserPasswordQuery (
    val key: String,
    val password: String
)