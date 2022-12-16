package com.mobilicos.smotrofon.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobilicos.smotrofon.data.CoursesLessonStepsConverters
import com.mobilicos.smotrofon.data.LessonStepsConverters
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.models.Item

import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import com.mobilicos.smotrofon.room.dao.LessonDao


@Database(entities = [Item::class, CourseLesson::class], version = 1)
@TypeConverters(LessonStepsConverters::class, CoursesLessonStepsConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coursesLessonDao(): CoursesLessonDao
    abstract fun lessonDao(): LessonDao
}
