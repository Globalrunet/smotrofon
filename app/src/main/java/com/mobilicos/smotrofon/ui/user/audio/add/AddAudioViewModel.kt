package com.mobilicos.smotrofon.ui.user.audio.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.repositories.AudioRepository
import com.mobilicos.smotrofon.data.formstate.UpdateAudioFormState
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.responses.UpdateAudioDataResponse
import com.mobilicos.smotrofon.data.responses.UploadAudioPosterResponse
import com.mobilicos.smotrofon.data.responses.UploadAudioResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAudioViewModel @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {

    var key: String = ""
    var currentVideoItem: Media? = null
    var image: String = ""

    var currentVideoPath: String? = null
    var currentImagePath: String? = null
    var videoId: Int = -1

    var needToRefreshData: Boolean = false
    private val _updateVideoFormState = MutableStateFlow<UpdateAudioFormState?>(null)
    val updateVideoFormState: StateFlow<UpdateAudioFormState?> = _updateVideoFormState

    private val _updateVideoResult = MutableStateFlow<Result<UpdateAudioDataResponse>>(Result.ready())
    val updateVideoResult: StateFlow<Result<UpdateAudioDataResponse>> = _updateVideoResult

    private val _uploadMediaPosterResult = MutableStateFlow<Result<UploadAudioPosterResponse>>(Result.ready())
    val uploadMediaPosterResult: StateFlow<Result<UploadAudioPosterResponse>> = _uploadMediaPosterResult

    private val _uploadVideoResult = MutableStateFlow<Result<UploadAudioResponse>>(Result.ready())
    val uploadVideoResult: StateFlow<Result<UploadAudioResponse>> = _uploadVideoResult

    fun uploadMediaPoster() {
        viewModelScope.launch {
            if (videoId > 0) {
                val q = UploadAudioPosterQuery(id = videoId, key = key, type = Config.TYPE_VIDEO, image = image)
                repository.uploadAudioPoster(q = q).collect {
                    _uploadMediaPosterResult.value = it
                }
            }
        }
    }

    fun updateVideoData() {

        println("UPDATE DATTA")
        viewModelScope.launch {
            val query = EditAudioQuery(id = _updateVideoFormState.value?.id?:0,
                key = _updateVideoFormState.value?.key?:"",
                title = _updateVideoFormState.value?.title?:"",
                text = _updateVideoFormState.value?.text?:"",
                image = _updateVideoFormState.value?.image?:"")

            repository.updateAudioData(q = query).collect {
                _updateVideoResult.value = it
            }
        }
    }

    fun uploadVideo(nextChunk: Long = 0, id: Int = 0) {
        viewModelScope.launch {
            currentVideoPath?.let { path ->
                val q = UploadAudioQuery(
                    id = id,
                    key = key,
                    path = currentVideoPath ?: "",
                    next_chunk = nextChunk
                )

                repository.uploadAudio(q = q).collect {
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

        _updateVideoFormState.value = UpdateAudioFormState(
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