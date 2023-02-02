package com.mobilicos.smotrofon.data.models

import androidx.room.*
import com.mobilicos.smotrofon.data.converters.CoursesLessonStepsConverters
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "courses_lessons")
data class CourseLesson (
    @PrimaryKey(autoGenerate = true)
    val localId: Int? = null,
    @ColumnInfo val id: Int,
    @ColumnInfo val ident: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val text: String,
    @ColumnInfo @Json(name = "show_times") val showTimes: Int = 0,
    @ColumnInfo @Json(name = "user_id") val userId: Int = 0,
    @ColumnInfo @Json(name = "user_full_name") val userFullName: String = "",
    @ColumnInfo @Json(name = "user_icon") val userIcon: String = "",
    @ColumnInfo val image: String,
    @ColumnInfo @Json(name = "image_extension") val imageExtension: String = "",
    @ColumnInfo @Json(name = "date_added") val dateAdded: String = "",
    @TypeConverters(CoursesLessonStepsConverters::class)
    @ColumnInfo val steps: List<CoursesStep>? = null
)
