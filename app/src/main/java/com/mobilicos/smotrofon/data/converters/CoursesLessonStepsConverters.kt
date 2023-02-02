package com.mobilicos.smotrofon.data.converters

import androidx.room.TypeConverter
import com.mobilicos.smotrofon.data.models.CoursesStep

/**
 * converts List to and from String
 */
class CoursesLessonStepsConverters {

    @TypeConverter
    fun listToString(values: List<CoursesStep>): String {
        val strList = mutableListOf<String>()
        values.forEach {
            strList.add(it.toString())
        }
        return strList.joinToString(",")
    }

    @TypeConverter
    fun stringToList(value: String): List<CoursesStep> {
        val intList = mutableListOf<CoursesStep>()
        value.split(",").forEach {

        }
        return intList
    }
}