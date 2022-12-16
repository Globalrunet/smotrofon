package com.mobilicos.smotrofon.data

import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.remote.CoursesLessonRemoteDataSource
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class CoursesLessonRepository @Inject constructor(
        private val coursesLessonRemoteDataSource: CoursesLessonRemoteDataSource,
        private val lessonDao: CoursesLessonDao
    ) {

    suspend fun downloadCoursesLessonByIdent(ident: Int): Flow<Result<ResponseBody>> {
        return flow {
            emit(Result.loading())
            val result = coursesLessonRemoteDataSource.downloadCoursesLessonByIdent(ident = ident)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    fun insertLesson(lesson: CourseLesson) = lessonDao.insert(lesson)

    fun getLessonsIdList() = lessonDao.getLessonsIdList()
}