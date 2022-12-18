package com.mobilicos.smotrofon.ui.lessons.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.data.queries.CommentsAddQuery
import com.mobilicos.smotrofon.data.queries.CommentsListQuery
import com.mobilicos.smotrofon.data.repositories.CommentRepository
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.sourses.CommentsListDataSource
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class CommentsListViewMode @Inject constructor(
    private val retrofit: Retrofit,
    private val repository: CommentRepository
) : ViewModel() {

    private val _queryData = MutableStateFlow(CommentsListQuery())
    private val queryData: StateFlow<CommentsListQuery> = _queryData.asStateFlow()

    private val _addCommentQueryData = MutableStateFlow<Result<CommentAddResponse>>(Result.ready())
    private val addCommentQueryData: StateFlow<Result<CommentAddResponse>> = _addCommentQueryData.asStateFlow()


    fun addComment(app_label: String, model: String, object_id: Int, text: String, parent_id: Int = 0) {
        val query = CommentsAddQuery(app_label = app_label,
            model = model,
            object_id = object_id,
            text = text,
            parent_id = parent_id)
        viewModelScope.launch {
            repository.addCommentData(q = query).collect {
                _addCommentQueryData.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val commentsByQueryData: StateFlow<PagingData<Comment>> = queryData
        .onEach { println("RESULT QUERY $it") }
        .map { q -> (::commentsPager)(q) }
        .flatMapLatest { pager -> pager.flow }
        .cachedIn(viewModelScope).stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    private fun commentsPager(query: CommentsListQuery): Pager<Int, Comment> {
        return getCommentsResultStream(query)
    }

    private fun getCommentsResultStream(query: CommentsListQuery): Pager<Int, Comment> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 25,
                initialLoadSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CommentsListDataSource(retrofit, query) }
        )
    }

    fun setQuery(app_label: String, model: String, object_id: Int, key: String) {
        _queryData.tryEmit(CommentsListQuery(app_label = app_label, model = model, object_id = object_id, key = key))
    }
}