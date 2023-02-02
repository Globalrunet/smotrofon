package com.mobilicos.smotrofon.ui.courses.locallessonslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.repositories.CoursesLessonRepository
import com.mobilicos.smotrofon.data.local.LocalCoursesLessonsListDataSource
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.room.dao.CoursesLessonDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalCoursesLessonsListViewModel @Inject constructor(
    private val lessonRepository: CoursesLessonRepository,
    private val lessonDao: CoursesLessonDao,) : ViewModel() {

    var changedPosition: Int = -1
    var language: String = Config.DEFAULT_LANGUAGE
    private val _queryData = MutableStateFlow(CoursesLessonsListQuery())
    private val queryData: StateFlow<CoursesLessonsListQuery> = _queryData.asStateFlow()

    private val _lessonsIdList = MutableStateFlow(listOf<Int>())

    var searchString: String = ""

    init {
        getLessonsIdList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<CourseLesson>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: CoursesLessonsListQuery): Pager<Int, CourseLesson> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: CoursesLessonsListQuery): Pager<Int, CourseLesson> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { LocalCoursesLessonsListDataSource(lessonDao = lessonDao, query = query) }
        )
    }

    fun setQuery(query: String) {
        _queryData.tryEmit(CoursesLessonsListQuery(query = query, language = language))
    }

    fun setCurrentLanguage(language: String) {
        this.language = language
        _queryData.tryEmit(CoursesLessonsListQuery(query = _queryData.value.query, language = language))
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