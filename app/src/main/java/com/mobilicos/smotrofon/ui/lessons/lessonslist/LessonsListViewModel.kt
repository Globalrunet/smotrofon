package com.mobilicos.smotrofon.ui.lessons.lessonslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.CoursesLessonRepository
import com.mobilicos.smotrofon.data.LessonRepository
import com.mobilicos.smotrofon.data.models.CoursesLessonsListQuery
import com.mobilicos.smotrofon.data.remote.LessonsListDataSource
import com.mobilicos.smotrofon.data.models.Lesson
import com.mobilicos.smotrofon.data.models.LessonsListQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class LessonsListViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val retrofit: Retrofit) : ViewModel() {

    var changedPosition: Int = -1
    var language: String = Config.DEFAULT_LANGUAGE
    var currentProject: Int = 0

    private val _queryData = MutableStateFlow(LessonsListQuery())
    private val queryData: StateFlow<LessonsListQuery> = _queryData.asStateFlow()

    private val _lessonsIdList = MutableStateFlow(listOf<Int>())
    val lessonsIdList: StateFlow<List<Int>> = _lessonsIdList.asStateFlow()

    public var searchString: String = ""

    init {
        getLessonsIdList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessonsByQueryData: StateFlow<PagingData<Lesson>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::lessonsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun lessonsPager(query: LessonsListQuery): Pager<Int, Lesson> {
        return getSearchResultStream(query)
    }

    private fun getSearchResultStream(query: LessonsListQuery): Pager<Int, Lesson> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { LessonsListDataSource(retrofit, query) }
        )
    }

    fun setQuery(query: String) {
        searchString = query
        _queryData.tryEmit(LessonsListQuery(query = query, language = language, project = currentProject))
    }

    fun setCurrentLanguage(language: String) {
        this.language = language
        _queryData.tryEmit(LessonsListQuery(query = _queryData.value.query, language = language, project = currentProject))
    }

    fun setCurrentProjectIdent(project: Int = 0) {
        this.currentProject = project
        _queryData.tryEmit(LessonsListQuery(query = _queryData.value.query, language = language, project = project))
    }

    fun getLessonsIdList() {
        viewModelScope.launch(Dispatchers.IO) {
            _lessonsIdList.value = lessonRepository.getLessonsIdList()
        }
    }
}