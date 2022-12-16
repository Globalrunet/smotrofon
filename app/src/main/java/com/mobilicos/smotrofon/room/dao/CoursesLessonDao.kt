package com.mobilicos.smotrofon.room.dao

import androidx.recyclerview.widget.ConcatAdapter
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.CourseLesson

@Dao
interface CoursesLessonDao {
    @Query("SELECT * from courses_lessons ORDER BY title ASC")
    fun getAlphabetizedLessonsList(): List<CourseLesson>?

    @Query("SELECT * from courses_lessons WHERE title LIKE '%' || :query || '%' ORDER BY id ASC LIMIT :size OFFSET :page-1*:size")
    fun getPagedLessonsListWithQuery(page: Int = 1, query: String = "", size: Int = Config.DEFAULT_PAGE_SIZE): List<CourseLesson>

    @Query("SELECT * from courses_lessons ORDER BY id ASC LIMIT :size OFFSET :page-1*:size")
    fun getPagedLessonsList(page: Int = 1, size: Int = Config.DEFAULT_PAGE_SIZE): List<CourseLesson>

    @Query("SELECT COUNT(*) from courses_lessons")
    fun getLessonsListCount(): Int

    @Query("SELECT COUNT(*) from courses_lessons WHERE title LIKE '%' || :query || '%'")
    fun getLessonsListCountWithQuery(query: String = ""): Int

    @Query("SELECT * from courses_lessons ORDER BY id ASC")
    fun getIdLessonsList(): List<CourseLesson>?

    @Query("SELECT id from courses_lessons ORDER BY id ASC")
    fun getLessonsIdList(): List<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(lesson: CourseLesson)

    @Query("DELETE FROM courses_lessons")
    fun deleteAll()
}