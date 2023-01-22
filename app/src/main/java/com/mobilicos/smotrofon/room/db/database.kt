package com.mobilicos.smotrofon.room.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobilicos.smotrofon.data.CoursesLessonStepsConverters
import com.mobilicos.smotrofon.data.DateConverter
import com.mobilicos.smotrofon.data.LessonStepsConverters
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.room.dao.BlockedUserDao
import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import com.mobilicos.smotrofon.room.dao.LessonDao
import com.mobilicos.smotrofon.room.entities.BlockedUser


@Database(entities = [Item::class, CourseLesson::class, BlockedUser::class], version = 2, exportSchema = true)
@TypeConverters(LessonStepsConverters::class, CoursesLessonStepsConverters::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coursesLessonDao(): CoursesLessonDao
    abstract fun lessonDao(): LessonDao
    abstract fun blockedUserDao(): BlockedUserDao
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " +
                "`blocked_user` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`userId` INTEGER NOT NULL, " +
                "`blockedUserId` INTEGER NOT NULL, " +
                "`dateAdded` INTEGER NOT NULL)")
    }
}