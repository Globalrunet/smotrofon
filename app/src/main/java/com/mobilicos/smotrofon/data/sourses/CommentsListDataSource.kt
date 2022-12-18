package com.mobilicos.smotrofon.data.sourses

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobilicos.smotrofon.data.models.Comment
import retrofit2.HttpException
import com.mobilicos.smotrofon.data.queries.CommentsListQuery
import com.mobilicos.smotrofon.network.services.CommentService
import retrofit2.Retrofit
import javax.inject.Inject

class CommentsListDataSource @Inject constructor(
    private val retrofit: Retrofit,
    private val query: CommentsListQuery
) : PagingSource<Int, Comment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {

        println("RESULT SOURCE $query")

        val page = params.key ?: 1

        val service = retrofit.create(CommentService::class.java)

        try {
            val response = service.getCommentsListData(page = page,
                app_label = query.app_label,
                model = query.model,
                object_id = query.object_id,
                size = params.loadSize)

            println("INSIDE getCommentsListData $response")

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
            println("INSIDE HttpException $e")
            return LoadResult.Error(e)
        } catch (e: Exception) {

            println("INSIDE Exception $e")
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null

        println("RESULT REFRESH::: $anchorPosition / $page")

        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}