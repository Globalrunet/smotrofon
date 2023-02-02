package com.mobilicos.smotrofon.ui.user.audio.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.repositories.AudioRepository
import com.mobilicos.smotrofon.data.formstate.UpdateAudioFormState
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.EditAudioQuery
import com.mobilicos.smotrofon.data.queries.UploadAudioPosterQuery
import com.mobilicos.smotrofon.data.responses.UpdateAudioDataResponse
import com.mobilicos.smotrofon.data.responses.UploadAudioPosterResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    var key: String = ""
    var currentVideoItem: Audio? = null
    var image: String = ""
    var needToRefreshData: Boolean = false
    private val _updateVideoFormState = MutableStateFlow(UpdateAudioFormState())
    val updateVideoFormState: StateFlow<UpdateAudioFormState> = _updateVideoFormState

    private val _updateVideoResult = MutableStateFlow<Result<UpdateAudioDataResponse>>(Result.ready())
    val updateVideoResult: StateFlow<Result<UpdateAudioDataResponse>> = _updateVideoResult

    private val _uploadMediaPosterResult = MutableStateFlow<Result<UploadAudioPosterResponse>>(Result.ready())
    val uploadMediaPosterResult: StateFlow<Result<UploadAudioPosterResponse>> = _uploadMediaPosterResult

    fun uploadMediaPoster() {
        viewModelScope.launch {
            currentVideoItem?.let {media ->
                val q = UploadAudioPosterQuery(id = media.id, key = key, type = Config.TYPE_VIDEO, image = image)
                audioRepository.uploadAudioPoster(q = q).collect {
                    _uploadMediaPosterResult.value = it
                }
            }
        }
    }

    fun updateVideoData() {

        println("UPDATE DATTA")
        viewModelScope.launch {
            val query = EditAudioQuery(id = _updateVideoFormState.value.id,
                key = _updateVideoFormState.value.key,
                title = _updateVideoFormState.value.title,
                text = _updateVideoFormState.value.text,
                image = _updateVideoFormState.value.image)

            audioRepository.updateAudioData(q = query).collect {
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

        _updateVideoFormState.value = UpdateAudioFormState(
            id = currentVideoItem?.id ?: -1,
            key = key,
            titleError = if (isTitleValid) null else R.string.edit_audio_invalid_title,
            textError = if (isTextValid) null else R.string.edit_audio_invalid_text,
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