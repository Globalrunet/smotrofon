package com.mobilicos.smotrofon.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import com.mobilicos.smotrofon.room.dao.LessonDao
import com.mobilicos.smotrofon.room.db.AppDatabase

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "database.db"
        ).build()
    }

    @Provides
    fun provideLessonDao(appDatabase: AppDatabase): LessonDao {
        return appDatabase.lessonDao()
    }

    @Provides
    fun provideCoursesLessonDao(appDatabase: AppDatabase): CoursesLessonDao {
        return appDatabase.coursesLessonDao()
    }
}