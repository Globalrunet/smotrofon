package com.mobilicos.smotrofon.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.Channel
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.network.services.MediaService
import retrofit2.Retrofit
import javax.inject.Inject

class ChannelsListDataSource @Inject constructor(
    private val retrofit: Retrofit,
    private val query: String = "") : PagingSource<Int, Channel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Channel> {

        println("RESULT SOURCE $query")

        val page = params.key ?: 1

        val service = retrofit.create(MediaService::class.java)

        try {
            val response = service.getChannelsListData(page=page, query=query)

            return if (response.isSuccessful) {
                val mediaElements = checkNotNull(response.body()).elements
                val totalPages = checkNotNull(response.body()).totalPages
                val nextPage = if (page < totalPages) page + 1 else null
                val prevPage = if (page > 1) page - 1 else null
                LoadResult.Page(
                    data = mediaElements,
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

    override fun getRefreshKey(state: PagingState<Int, Channel>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null

        println("RESULT REFRESH::: $anchorPosition / $page")

        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}