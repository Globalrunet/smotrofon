package com.mobilicos.smotrofon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.network.services.VideoService
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.max

// The initial key used for loading.
// This is the article id of the first article that will be loaded
private const val STARTING_KEY = 0
private const val LOAD_DELAY_MILLIS = 3_000L
private val firstArticleCreatedTime = LocalDateTime.now()

class  VideoRemotePagingDataSource @Inject constructor(
    private val service: VideoService,
    private val query: String
): PagingSource<Int, Media>() {

    /**
     * Makes sure the paging key is never less than [STARTING_KEY]
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {

        delay(1000)

        val page: Int = params.key ?: 1
        val pageSize: Int = params.loadSize

        val response = service.getHiltVideoListData(page=page, size=pageSize)

        if (response.isSuccessful) {

            val videos = checkNotNull(response.body()).elements
            return LoadResult.Page(
                data = videos,
                nextKey = if (videos.size < pageSize) null else page + 1,
                prevKey = if (page == 1) null else page - 1,
            )
        } else {
            return LoadResult.Error(HttpException(response))
        }
    }
}