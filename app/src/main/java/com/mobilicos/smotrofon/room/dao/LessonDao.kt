package com.mobilicos.smotrofon.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.models.Item

@Dao
interface LessonDao {
    @Query("SELECT * from lessons ORDER BY title ASC")
    fun getAlphabetizedLessonsList(): List<Item>?

    @Query("SELECT * from lessons WHERE title LIKE '%' || :query || '%' ORDER BY id ASC LIMIT :size OFFSET :page-1*:size")
    fun getPagedLessonsListWithQuery(page: Int = 1, query: String = "", size: Int = Config.DEFAULT_PAGE_SIZE): List<Item>

    @Query("SELECT * from lessons ORDER BY id ASC LIMIT :size OFFSET :page-1*:size")
    fun getPagedLessonsList(page: Int = 1, size: Int = Config.DEFAULT_PAGE_SIZE): List<Item>

    @Query("SELECT COUNT(*) from lessons")
    fun getLessonsListCount(): Int

    @Query("SELECT COUNT(*) from lessons WHERE title LIKE '%' || :query || '%'")
    fun getLessonsListCountWithQuery(query: String = ""): Int

    @Query("SELECT * from lessons ORDER BY id ASC")
    fun getIdLessonsList(): List<Item>?

    @Query("SELECT id from lessons ORDER BY id ASC")
    fun getLessonsIdList(): List<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(lesson: Item)

    @Query("DELETE FROM lessons")
    fun deleteAll()
}