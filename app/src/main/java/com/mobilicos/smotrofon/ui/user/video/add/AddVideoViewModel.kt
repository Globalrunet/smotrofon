package com.mobilicos.smotrofon.ui.user.video.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.VideoRepository
import com.mobilicos.smotrofon.data.formstate.UpdateVideoFormState
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.EditVideoQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.data.queries.UploadVideoQuery
import com.mobilicos.smotrofon.data.responses.UploadVideoResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    var key: String = ""
    var currentVideoItem: Media? = null
    var image: String = ""

    var currentVideoPath: String? = null
    var currentImagePath: String? = null
    var videoId: Int = -1

    var needToRefreshData: Boolean = false
    private val _updateVideoFormState = MutableStateFlow<UpdateVideoFormState?>(null)
    val updateVideoFormState: StateFlow<UpdateVideoFormState?> = _updateVideoFormState

    private val _updateVideoResult = MutableStateFlow<Result<UpdateVideoDataResponse>>(Result.ready())
    val updateVideoResult: StateFlow<Result<UpdateVideoDataResponse>> = _updateVideoResult

    private val _uploadMediaPosterResult = MutableStateFlow<Result<UploadMediaPosterResponse>>(Result.ready())
    val uploadMediaPosterResult: StateFlow<Result<UploadMediaPosterResponse>> = _uploadMediaPosterResult

    private val _uploadVideoResult = MutableStateFlow<Result<UploadVideoResponse>>(Result.ready())
    val uploadVideoResult: StateFlow<Result<UploadVideoResponse>> = _uploadVideoResult

    fun uploadMediaPoster() {
        viewModelScope.launch {
            if (videoId > 0) {
                val q = UploadMediaPosterQuery(id = videoId, key = key, type = Config.TYPE_VIDEO, image = image)
                videoRepository.uploadVideoPoster(q = q).collect {
                    _uploadMediaPosterResult.value = it
                }
            }
        }
    }

    fun updateVideoData() {

        println("UPDATE DATTA")
        viewModelScope.launch {
            val query = EditVideoQuery(id = _updateVideoFormState.value?.id?:0,
                key = _updateVideoFormState.value?.key?:"",
                title = _updateVideoFormState.value?.title?:"",
                text = _updateVideoFormState.value?.text?:"",
                image = _updateVideoFormState.value?.image?:"")

            videoRepository.updateVideoData(q = query).collect {
                _updateVideoResult.value = it
            }
        }
    }

    fun uploadVideo(nextChunk: Long = 0, id: Int = 0) {
        viewModelScope.launch {
            currentVideoPath?.let { path ->
                val q = UploadVideoQuery(
                    id = id,
                    key = key,
                    path = currentVideoPath ?: "",
                    next_chunk = nextChunk
                )

                videoRepository.uploadVideo(q = q).collect {
                    _uploadVideoResult.value = it
                }
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
            id = videoId,
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