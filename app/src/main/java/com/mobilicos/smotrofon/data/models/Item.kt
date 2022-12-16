package com.mobilicos.smotrofon.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mobilicos.smotrofon.data.LessonStepsConverters
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "lessons")
data class Item (
    @PrimaryKey(autoGenerate = true)
    val localId: Int? = null,
    @ColumnInfo val id: Int,
    @ColumnInfo val ident: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val text: String,
    @ColumnInfo @Json(name = "steps_count") val stepsCount: Int,
    @ColumnInfo @Json(name = "animation_interval") val animationInterval: Long = 500,
    @ColumnInfo @Json(name = "icon_extension") val iconExtension: String,
    @TypeConverters(LessonStepsConverters::class)
    @ColumnInfo val steps: List<Step>
)
