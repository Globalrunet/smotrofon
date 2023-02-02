package com.mobilicos.smotrofon.ui.channels.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.data.remote.ChannelsListDataSource
import com.mobilicos.smotrofon.data.models.Channel
import com.mobilicos.smotrofon.data.models.ChannelQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val retrofit: Retrofit
) : ViewModel() {

    private val _queryData = MutableStateFlow(ChannelQuery(query=""))
    private val queryData: StateFlow<ChannelQuery> = _queryData.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val channelsByQueryData: StateFlow<PagingData<Channel>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::channelsPager)(q.query) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun channelsPager(query: String): Pager<Int, Channel> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: String): Pager<Int, Channel> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ChannelsListDataSource(retrofit, query) }
        )
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(ChannelQuery(query = query))
    }
}