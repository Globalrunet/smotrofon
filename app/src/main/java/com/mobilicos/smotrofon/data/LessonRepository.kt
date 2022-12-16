package com.mobilicos.smotrofon.data

import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.data.remote.LessonRemoteDataSource
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.room.dao.LessonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class LessonRepository @Inject constructor(
        private val lessonRemoteDataSource: LessonRemoteDataSource,
        private val lessonDao: LessonDao
    ) {

    suspend fun downloadLessonByIdent(ident: Int): Flow<Result<ResponseBody>> {
        return flow {
            emit(Result.loading())
            val result = lessonRemoteDataSource.downloadLessonByIdent(ident=ident)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    fun insertLesson(lesson: Item) = lessonDao.insert(lesson)

    fun getLessonsIdList() = lessonDao.getLessonsIdList()
}