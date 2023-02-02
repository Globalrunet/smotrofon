package com.mobilicos.smotrofon.ui.lessons.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.data.queries.CommentsAddQuery
import com.mobilicos.smotrofon.data.queries.CommentsEditQuery
import com.mobilicos.smotrofon.data.queries.CommentsListQuery
import com.mobilicos.smotrofon.data.queries.CommentsRemoveQuery
import com.mobilicos.smotrofon.data.repositories.CommentRepository
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.responses.CommentEditResponse
import com.mobilicos.smotrofon.data.responses.CommentRemoveResponse
import com.mobilicos.smotrofon.data.remote.CommentsListDataSource
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

    var isRemoveDialogShown = false
    var currentElement: Comment? = null
    var currentPosition: Int = -1
    lateinit var currentAppLabel: String
    lateinit var currentModel: String
    private var currentObjectId: Int = 0
    private var images: MutableMap<String, Pair<String, String>> = mutableMapOf()

    private val _queryData = MutableStateFlow(CommentsListQuery())
    private val queryData: StateFlow<CommentsListQuery> = _queryData.asStateFlow()

    private val _addCommentResponseData = MutableStateFlow<Result<CommentAddResponse>>(Result.ready())
    val addCommentResponseData: StateFlow<Result<CommentAddResponse>> = _addCommentResponseData.asStateFlow()

    private val _removeCommentResponseData = MutableStateFlow<Result<CommentRemoveResponse>>(Result.ready())
    val removeCommentResponseData: StateFlow<Result<CommentRemoveResponse>> = _removeCommentResponseData.asStateFlow()

    private val _editCommentResponseData = MutableStateFlow<Result<CommentEditResponse>>(Result.ready())
    val editCommentResponseData: StateFlow<Result<CommentEditResponse>> = _editCommentResponseData.asStateFlow()

    fun setObjectData(app_label: String, model: String, object_id: Int) {
        currentAppLabel = app_label
        currentModel = model
        currentObjectId = object_id
    }

    fun addImage(key: String, image: String, icon: String) {
        val pair = Pair(first = image, second = icon)
        images[key] = pair
    }

    fun getImagesList(): List<String> {
        return images.keys.toList()
    }

    fun removeImage(key: String) {
        images.remove(key)
    }

    fun removeAllImage() {
        images.clear()
    }

    fun addComment(key: String, text: String, images: List<String>, parent_id: Int = 0) {
        val query = CommentsAddQuery(key = key,
            app_label = currentAppLabel,
            model = currentModel,
            object_id = currentObjectId,
            text = text,
            parent_id = parent_id,
            images = images)
        viewModelScope.launch {
            repository.addCommentData(q = query).collect {
                _addCommentResponseData.value = it
            }
        }
    }

    fun removeComment(key: String, comment_id: Int) {
        val query = CommentsRemoveQuery(key = key,
            comment_id = comment_id)
        viewModelScope.launch {
            repository.removeCommentData(q = query).collect {
                _removeCommentResponseData.value = it
            }
        }
    }

    fun editComment(key: String, text: String, comment_id: Int) {
        val query = CommentsEditQuery(key = key, text = text, comment_id = comment_id)
        viewModelScope.launch {
            repository.editCommentData(q = query).collect {
                _editCommentResponseData.value = it
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

    fun setQuery(key: String) {
        _queryData.tryEmit(CommentsListQuery(app_label = currentAppLabel,
            model = currentModel,
            object_id = currentObjectId,
            key = key))
    }

    fun clearAddCommentResult() {
        _addCommentResponseData.value = Result.ready()
    }

    fun clearEditCommentResult() {
        _editCommentResponseData.value = Result.ready()
    }

    fun clearRemoveCommentResult() {
        _removeCommentResponseData.value = Result.ready()
    }
}