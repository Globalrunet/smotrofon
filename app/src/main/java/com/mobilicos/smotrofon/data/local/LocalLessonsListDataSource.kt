package com.mobilicos.smotrofon.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobilicos.smotrofon.data.models.*
import retrofit2.HttpException
import com.mobilicos.smotrofon.room.dao.LessonDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Math.ceil
import javax.inject.Inject

class LocalLessonsListDataSource @Inject constructor(
    private val lessonDao: LessonDao,
    private val query: LessonsListQuery
) : PagingSource<Int, Item>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {

        println("RESULT SOURCE $query")

        val page = params.key ?: 1

        try {
            val response = withContext(Dispatchers.IO) {
                if (query.query.isNotEmpty()) {
                    lessonDao.getPagedLessonsListWithQuery(
                        page = page,
                        query = query.query,
                        size = params.loadSize)
                } else {
                    lessonDao.getPagedLessonsList(
                        page = page,
                        size = params.loadSize)
                }
            }

            val totalPages = withContext(Dispatchers.IO) {
                if (query.query.isNotEmpty()) {
                    kotlin.math.ceil(
                        lessonDao.getLessonsListCountWithQuery(query = query.query)
                            .toDouble() / params.loadSize
                    )
                } else {
                    kotlin.math.ceil(lessonDao.getLessonsListCount().toDouble() / params.loadSize)
                }
            }

            println("LESSONS total pages // $totalPages")
            println("LESSONS  ${response.size}")

            val nextPage = if (page < totalPages) page + 1 else null
            val prevPage = if (page > 1) page - 1 else null

            return LoadResult.Page(
                data = response,
                prevKey = prevPage,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            println("LESSONS ERROR $e")
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null

        println("RESULT REFRESH::: $anchorPosition / $page")

        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}