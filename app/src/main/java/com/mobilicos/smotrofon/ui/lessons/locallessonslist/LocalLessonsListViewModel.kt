package com.mobilicos.smotrofon.ui.lessons.locallessonslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.CoursesLessonRepository
import com.mobilicos.smotrofon.data.LessonRepository
import com.mobilicos.smotrofon.data.local.LocalCoursesLessonsListDataSource
import com.mobilicos.smotrofon.data.local.LocalLessonsListDataSource
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import com.mobilicos.smotrofon.room.dao.LessonDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class LocalLessonsListViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val lessonDao: LessonDao,) : ViewModel() {

    var changedPosition: Int = -1
    var language: String = Config.DEFAULT_LANGUAGE
    private val _queryData = MutableStateFlow(LessonsListQuery())
    private val queryData: StateFlow<LessonsListQuery> = _queryData.asStateFlow()

    private val _lessonsIdList = MutableStateFlow(listOf<Int>())

    var searchString: String = ""

    init {
        getLessonsIdList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<Item>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: LessonsListQuery): Pager<Int, Item> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: LessonsListQuery): Pager<Int, Item> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { LocalLessonsListDataSource(lessonDao = lessonDao, query = query) }
        )
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(LessonsListQuery(query = query, language = language))
    }

    fun setCurrentLanguage(language: String) {
        this.language = language
        _queryData.tryEmit(LessonsListQuery(query = _queryData.value.query, language = language))
    }

    fun emitData() {
        val q = _queryData.value
        q.version += 1
        _queryData.tryEmit(q)
    }

    fun getLessonsIdList() {
        viewModelScope.launch(Dispatchers.IO) {
            _lessonsIdList.value = lessonRepository.getLessonsIdList()
        }
    }
}