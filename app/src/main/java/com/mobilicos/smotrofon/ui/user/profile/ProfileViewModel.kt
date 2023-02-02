package com.mobilicos.smotrofon.ui.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.data.models.User
import com.mobilicos.smotrofon.data.repositories.UserRepository
import com.mobilicos.smotrofon.data.responses.MediaRepository
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _getUserDataResponseData = MutableStateFlow<Result<User>>(Result.ready())
    val getUserDataResponseData: StateFlow<Result<User>> = _getUserDataResponseData.asStateFlow()

    fun getUserData(id: Int) {
        viewModelScope.launch {
            userRepository.getUserData(id = id).collect {
                _getUserDataResponseData.value = it
            }
        }
    }
}
