package com.mobilicos.smotrofon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import com.mobilicos.smotrofon.data.models.Lesson
import com.mobilicos.smotrofon.data.models.LessonsListQuery
import com.mobilicos.smotrofon.network.services.LessonService
import retrofit2.Retrofit
import javax.inject.Inject

class LessonsListDataSource @Inject constructor(
    private val retrofit: Retrofit,
    private val query: LessonsListQuery) : PagingSource<Int, Lesson>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Lesson> {

        println("RESULT SOURCE $query")

        var page = params.key ?: 1

        val service = retrofit.create(LessonService::class.java)

        try {
            val response = service.getLessonsListData(page = page,
                query = query.query,
                project = query.project,
                language = query.language,
                size = params.loadSize)

            return if (response.isSuccessful) {
                val elements = checkNotNull(response.body()).elements
                val totalPages = checkNotNull(response.body()).totalPages
                val nextPage = if (page < totalPages) page + 1 else null
                val prevPage = if (page > 1) page - 1 else null
                LoadResult.Page(
                    data = elements,
                    prevKey = prevPage,
                    nextKey = nextPage
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Lesson>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null

        println("RESULT REFRESH::: $anchorPosition / $page")

        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}