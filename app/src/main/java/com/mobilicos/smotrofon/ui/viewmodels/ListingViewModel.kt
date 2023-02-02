package com.mobilicos.smotrofon.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.data.repositories.AudioRepository
import com.mobilicos.smotrofon.data.responses.MediaRepository
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * ViewModel for ListingActivity
 */
@HiltViewModel
class ListingViewModel @Inject constructor(
    private val videoRepository: MediaRepository,
    private val audioRepository: AudioRepository
    ) :ViewModel() {

    private val _videoSuggestionsList = MutableLiveData<Result<SuggestionResponse>>()
    val videoSuggestionsList = _videoSuggestionsList

    private val _audioSuggestionsList = MutableLiveData<Result<SuggestionResponse>>()
    val audioSuggestionsList = _audioSuggestionsList

    fun fetchVideoSuggestions(q: String) {
        viewModelScope.launch {
            videoRepository.fetchVideosSuggestions(q).collect {
                _videoSuggestionsList.value = it
            }
        }
    }

    fun fetchAudioSuggestions(q: String) {
        viewModelScope.launch {
            audioRepository.fetchVideosSuggestions(q).collect {
                _audioSuggestionsList.value = it
            }
        }
    }
}