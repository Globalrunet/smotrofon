package com.mobilicos.smotrofon.data

import androidx.room.TypeConverter
import com.mobilicos.smotrofon.data.models.Step

/**
 * converts List to and from String
 */
class LessonStepsConverters {

    @TypeConverter
    fun listToString(values: List<Step>): String {
        val strList = mutableListOf<String>()
        values.forEach {
            strList.add(it.toString())
        }
        return strList.joinToString(",")
    }

    @TypeConverter
    fun stringToList(value: String): List<Step> {
        val intList = mutableListOf<Step>()
        value.split(",").forEach {

        }
        return intList
    }

}