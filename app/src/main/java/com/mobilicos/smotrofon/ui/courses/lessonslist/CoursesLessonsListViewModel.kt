package com.mobilicos.smotrofon.ui.courses.lessonslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.repositories.CoursesLessonRepository
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.remote.CoursesLessonsListDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class CoursesLessonsListViewModel @Inject constructor(
    private val lessonRepository: CoursesLessonRepository,
    private val retrofit: Retrofit) : ViewModel() {

    var changedPosition: Int = -1
    var language: String = Config.DEFAULT_LANGUAGE
    private val _queryData = MutableStateFlow(CoursesLessonsListQuery())
    private val queryData: StateFlow<CoursesLessonsListQuery> = _queryData.asStateFlow()

    private val _lessonsIdList = MutableStateFlow(listOf<Int>())
    val lessonsIdList: StateFlow<List<Int>> = _lessonsIdList.asStateFlow()

    var searchString: String = ""

    init {
        getLessonsIdList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<CourseLessonListItem>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: CoursesLessonsListQuery): Pager<Int, CourseLessonListItem> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: CoursesLessonsListQuery): Pager<Int, CourseLessonListItem> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CoursesLessonsListDataSource(retrofit, query) }
        )
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(CoursesLessonsListQuery(query = query, language = language))
    }

    fun setCurrentLanguage(language: String) {
        this.language = language
        _queryData.tryEmit(CoursesLessonsListQuery(query = _queryData.value.query, language = language))
    }

    fun getLessonsIdList() {
        viewModelScope.launch(Dispatchers.IO) {
            _lessonsIdList.value = lessonRepository.getLessonsIdList()
        }
    }
}