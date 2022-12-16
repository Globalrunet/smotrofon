package com.mobilicos.smotrofon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.VideoListDataSource
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.data.models.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val retrofit: Retrofit
) : ViewModel() {

    private var counter = 0
    private val _query = MutableStateFlow("")
    private val _type = MutableStateFlow(Config.TYPE_VIDEO)

    private val _queryData = MutableStateFlow(Query(query=_query.value, type=_type.value))
    private val queryData: StateFlow<Query> = _queryData.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaByQueryData: StateFlow<PagingData<Media>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::mediaPager)(q.query, q.type) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun mediaPager(query: String, type: Int): Pager<Int, Media> {
        return getSearchResultStream(query, type)
    }

    private fun getSearchResultStream(query: String, t: Int): Pager<Int, Media> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { VideoListDataSource(retrofit, query, t) }
        )
    }

    fun setQuery(query: String) {
        _query.tryEmit(query)
        _queryData.tryEmit(Query(query = query, type = _type.value))
    }

    fun setContentType(type: Int) {
        _type.tryEmit(type)
        _queryData.tryEmit(Query(query = _query.value, type = type))
    }

    fun getContentType(): Int {
        return _type.value
    }

    fun increaseCounter() {
        counter ++
    }
}