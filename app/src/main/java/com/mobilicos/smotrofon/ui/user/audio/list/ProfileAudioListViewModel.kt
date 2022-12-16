package com.mobilicos.smotrofon.ui.user.audio.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.AudioRepository
import com.mobilicos.smotrofon.data.ProfileMediaListDataSource
import com.mobilicos.smotrofon.data.VideoRepository
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.AudioListQuery
import com.mobilicos.smotrofon.data.queries.RemoveAudioQuery
import com.mobilicos.smotrofon.data.queries.RemoveMediaQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.data.responses.RemoveAudioResponse
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.data.sourses.ProfileAudioListDataSource
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class ProfileAudioListViewModel @Inject constructor(
    private val retrofit: Retrofit,
    private val repository: AudioRepository
) : ViewModel() {

    var searchString: String = ""
    var isRemoveDialogShown = false
    var currentElement: Audio? = null
    var currentPosition: Int = -1
    lateinit var key: String

    private val _queryData = MutableStateFlow(AudioListQuery())
    private val queryData: StateFlow<AudioListQuery> = _queryData.asStateFlow()

    private val _removeVideoResult = MutableStateFlow<Result<RemoveAudioResponse>>(Result.ready())
    val removeVideoResult: StateFlow<Result<RemoveAudioResponse>> = _removeVideoResult

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<Audio>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: AudioListQuery): Pager<Int, Audio> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: AudioListQuery): Pager<Int, Audio> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProfileAudioListDataSource(retrofit = retrofit, query = query) }
        )
    }

    fun removeVideo(id: Int) {
        viewModelScope.launch {
            val q = RemoveAudioQuery(id = id, key = key, type = Config.TYPE_VIDEO)
            repository.removeAudio(q = q).collect {
                _removeVideoResult.value = it
            }
        }
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(AudioListQuery(query = query, key = key))
    }

    fun clearRemoveVideoResult() {
        _removeVideoResult.value = Result.ready()
    }
}