package com.mobilicos.smotrofon.data.queries

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportAddQuery(
    val app_label: String = "",
    val model: String = "",
    val object_id: Int = 0,
    val report_reason: Int = -1,
    val key: String = "",
    val text: String = ""
)