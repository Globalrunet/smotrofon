package com.mobilicos.smotrofon.ui.user.video.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.ProfileMediaListDataSource
import com.mobilicos.smotrofon.data.VideoRepository
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.RemoveMediaQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class ProfileVideoListViewModel @Inject constructor(
    private val retrofit: Retrofit,
    private val repository: VideoRepository) : ViewModel() {

    var searchString: String = ""
    var isRemoveDialogShown = false
    var currentElement: Media? = null
    var currentPosition: Int = -1
    lateinit var key: String

    private val _queryData = MutableStateFlow(MediaListQuery())
    private val queryData: StateFlow<MediaListQuery> = _queryData.asStateFlow()

    private val _removeVideoResult = MutableStateFlow<Result<RemoveMediaResponse>>(Result.ready())
    val removeVideoResult: StateFlow<Result<RemoveMediaResponse>> = _removeVideoResult

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<Media>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: MediaListQuery): Pager<Int, Media> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: MediaListQuery): Pager<Int, Media> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProfileMediaListDataSource(retrofit = retrofit, query = query) }
        )
    }

    fun removeVideo(id: Int) {
        viewModelScope.launch {
            val q = RemoveMediaQuery(id = id, key = key, type = Config.TYPE_VIDEO)
            repository.removeVideo(q = q).collect {
                _removeVideoResult.value = it
            }
        }
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(MediaListQuery(query = query, key = key))
    }

    fun clearRemoveVideoResult() {
        _removeVideoResult.value = Result.ready()
    }
}