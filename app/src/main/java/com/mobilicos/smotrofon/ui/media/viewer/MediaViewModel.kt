package com.mobilicos.smotrofon.ui.media.viewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.other.GetRelatedAudiosApi
import com.mobilicos.smotrofon.data.responses.MediaRepository
import com.mobilicos.smotrofon.data.models.Audio
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.model.Result
import com.google.android.exoplayer2.ExoPlayer
import com.mobilicos.smotrofon.data.repositories.UserRepository
import com.mobilicos.smotrofon.data.responses.SubscribeUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val selectedMedia = MutableLiveData<Media>()
    private val selectedAudio = MutableLiveData<Audio>()
    private val relatedVideoList = MutableLiveData<List<Media>?>()
    private val relatedAudioList = MutableLiveData<List<Audio>?>()
    private val currentPosition = MutableLiveData<Long>()
    private val currentPlayer = MutableLiveData<ExoPlayer?>()
    private val isPlaying = MutableLiveData<Boolean?>()
    var currentGraph: String? = null

    private val _type = MutableStateFlow(Config.TYPE_VIDEO)
    val type: StateFlow<Int> = _type.asStateFlow()

    private val _subscribeUserResponseData = MutableStateFlow<Result<SubscribeUserResponse>>(Result.ready())
    val subscribeUserResponseData: StateFlow<Result<SubscribeUserResponse>> = _subscribeUserResponseData.asStateFlow()

    suspend fun loadRelatedAudioList() {
        relatedAudioList.value = selectedAudio.value?.let { GetRelatedAudiosApi().getAudiosData(it.id) }!!.elements
    }

    private val _mediaRelatedList = MutableLiveData<Result<MediaResponse>>()
    private val mediaRelatedList = _mediaRelatedList

    fun fetchRelatedMedia() {
        viewModelScope.launch {
            selectedMedia.value?.let { media ->
                mediaRepository.fetchRelatedMedia(mediaId=media.id, type=type.value).collect {
                    _mediaRelatedList.value = it
                }
            }
        }
    }

    fun subscribeUser(key: String, otherUserId: Int) {
        viewModelScope.launch {
            userRepository.subscribeUser(key = key, otherUserId = otherUserId).collect {
                _subscribeUserResponseData.value = it
            }
        }
    }

    fun getPlayer(): ExoPlayer? {
        return currentPlayer.value
    }

    fun setPlayer(player: ExoPlayer?) {
        currentPlayer.value = player
    }

    fun setIsPlaying(playing: Boolean) {
        isPlaying.value = playing
    }

    fun getIsPlaying():Boolean {
        return if (isPlaying.value != null) {
            isPlaying.value!!
        } else {
            false
        }
    }

    fun getCurrentPosition(): Long {
        return if (currentPosition.value != null) {
            currentPosition.value!!
        } else {
            0L
        }
    }

    fun setCurrentPosition(position: Long) {
        currentPosition.value = position
    }

    fun setRelatedVideoListEmpty() {
        relatedVideoList.value = null
    }

    fun getObservableProduct() : MutableLiveData<Result<MediaResponse>> {
        return mediaRelatedList
    }

    fun getAudioObservableProduct() : MutableLiveData<List<Audio>?> {
        return relatedAudioList
    }

    fun select(media: Media, type: Int = Config.TYPE_VIDEO) {
        selectedMedia.value = media
        _type.tryEmit(type)
    }

    fun getSelected(): LiveData<Media> {
        return selectedMedia
    }

    fun selectAudio(audio: Audio) {
        selectedAudio.value = audio
    }

    fun getSelectedAudio(): LiveData<Audio> {
        return selectedAudio
    }

    fun getContentType(): Int {
        return _type.value
    }

    fun clearSubscribeUserResponseData() {
        _subscribeUserResponseData.value = Result.ready()
    }
}
