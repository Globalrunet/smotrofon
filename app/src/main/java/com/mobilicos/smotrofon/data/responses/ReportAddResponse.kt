package com.mobilicos.smotrofon.data.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportAddResponse (
    val result: Boolean = false,
    val app_label: String =  "",
    val model: String = "",
    val object_id: Int = 0,
    val message: String = ""
)