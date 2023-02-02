package com.mobilicos.smotrofon.ui.user.video.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.repositories.VideoRepository
import com.mobilicos.smotrofon.data.formstate.UpdateVideoFormState
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.EditVideoQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditVideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    var key: String = ""
    var currentVideoItem: Media? = null
    var image: String = ""
    var needToRefreshData: Boolean = false
    private val _updateVideoFormState = MutableStateFlow(UpdateVideoFormState())
    val updateVideoFormState: StateFlow<UpdateVideoFormState> = _updateVideoFormState

    private val _updateVideoResult = MutableStateFlow<Result<UpdateVideoDataResponse>>(Result.ready())
    val updateVideoResult: StateFlow<Result<UpdateVideoDataResponse>> = _updateVideoResult

    private val _uploadMediaPosterResult = MutableStateFlow<Result<UploadMediaPosterResponse>>(Result.ready())
    val uploadMediaPosterResult: StateFlow<Result<UploadMediaPosterResponse>> = _uploadMediaPosterResult

    fun uploadMediaPoster() {
        viewModelScope.launch {
            currentVideoItem?.let {media ->
                val q = UploadMediaPosterQuery(id = media.id, key = key, type = Config.TYPE_VIDEO, image = image)
                videoRepository.uploadVideoPoster(q = q).collect {
                    _uploadMediaPosterResult.value = it
                }
            }
        }
    }

    fun updateVideoData() {

        println("UPDATE DATTA")
        viewModelScope.launch {
            val query = EditVideoQuery(id = _updateVideoFormState.value.id,
                key = _updateVideoFormState.value.key,
                title = _updateVideoFormState.value.title,
                text = _updateVideoFormState.value.text,
                image = _updateVideoFormState.value.image)

            videoRepository.updateVideoData(q = query).collect {
                _updateVideoResult.value = it
            }
        }
    }

    fun clearVideoResult() {
        _updateVideoResult.value = Result.ready()
    }

    fun clearUploadPosterResult() {
        _uploadMediaPosterResult.value = Result.ready()
    }

    fun formDataChanged(title: String, text: String) {

        val isTitleValid = isTitleValid(title)
        val isTextValid = isTextValid(text)

        _updateVideoFormState.value = UpdateVideoFormState(
            id = currentVideoItem?.id ?: -1,
            key = key,
            titleError = if (isTitleValid) null else R.string.edit_video_invalid_title,
            textError = if (isTextValid) null else R.string.edit_video_invalid_text,
            isDataValid = isTitleValid and isTextValid,
            title = title,
            text = text
        )
    }

    private fun isTitleValid(username: String): Boolean {
        return username.isNotBlank() && username.length <= Config.VIDEO_TITLE_MAX_LENGTH
    }

    private fun isTextValid(text: String): Boolean {
        return text.isNotBlank() && text.length <= Config.VIDEO_TEXT_MAX_LENGTH
    }
}