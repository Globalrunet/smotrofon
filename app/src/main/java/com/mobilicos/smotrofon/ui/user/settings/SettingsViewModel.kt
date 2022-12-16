package com.mobilicos.smotrofon.ui.user.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.formstate.UpdateUserPasswordFormState
import com.mobilicos.smotrofon.data.formstate.UpdateUsernamesFormState
import com.mobilicos.smotrofon.data.queries.UpdateUserPasswordQuery
import com.mobilicos.smotrofon.data.repositories.UserRepository
import com.mobilicos.smotrofon.data.queries.UpdateUsernamesQuery
import com.mobilicos.smotrofon.data.queries.UploadUserImageQuery
import com.mobilicos.smotrofon.data.responses.UpdateUserPasswordResponse
import com.mobilicos.smotrofon.data.responses.UpdateUsernamesResponse
import com.mobilicos.smotrofon.data.responses.UploadUserImageResponse
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    var key: String = ""
    var image: String? = null

    private val _updateUsernamesFormState = MutableStateFlow(UpdateUsernamesFormState())
    val updateUsernamesFormState: StateFlow<UpdateUsernamesFormState> = _updateUsernamesFormState

    private val _updatePasswordFormState = MutableStateFlow(UpdateUserPasswordFormState())
    val updatePasswordFormState: StateFlow<UpdateUserPasswordFormState> = _updatePasswordFormState

    private val _updateUsernamesResult = MutableStateFlow<Result<UpdateUsernamesResponse>>(Result.ready())
    val updateUsernamesResult: StateFlow<Result<UpdateUsernamesResponse>> = _updateUsernamesResult

    private val _updatePasswordResult = MutableStateFlow<Result<UpdateUserPasswordResponse>>(Result.ready())
    val updatePasswordResult: StateFlow<Result<UpdateUserPasswordResponse>> = _updatePasswordResult

    private val _uploadUserImageResult = MutableStateFlow<Result<UploadUserImageResponse>>(Result.ready())
    val uploadUserImageResult: StateFlow<Result<UploadUserImageResponse>> = _uploadUserImageResult

    fun uploadUserImage() {
        viewModelScope.launch {
            image?.let { it ->
                val q = UploadUserImageQuery(key = key, image = it)
                repository.uploadUserImage(q = q).collect {
                    _uploadUserImageResult.value = it
                }
            }
        }
    }

    fun updateUsernames() {

        println("UPDATE DATTA")
        viewModelScope.launch {
            val query = UpdateUsernamesQuery(
                key = key,
                firstName = _updateUsernamesFormState.value.firstName,
                lastName = _updateUsernamesFormState.value.lastName)

            repository.updateUsernames(query = query).collect {
                _updateUsernamesResult.value = it
            }
        }
    }

    fun updateUserPassword() {

        viewModelScope.launch {
            val query = UpdateUserPasswordQuery(
                key = key,
                password = _updatePasswordFormState.value.password
            )

            repository.updatePassword(query = query).collect {
                _updatePasswordResult.value = it
            }
        }
    }

    fun usernamesFormDataChanged(firstName: String, lastName: String, isInited: Boolean = false) {

        val isFirstNameValid = isFirstNameValid(firstName)
        val isLastNameValid = isLastNameValid(lastName)

        _updateUsernamesFormState.value = UpdateUsernamesFormState(
            isInited = isInited,
            firstNameError = if (isFirstNameValid) null else R.string.profile_settings_invalid_firstname,
            lastNameError = if (isLastNameValid) null else R.string.profile_settings_invalid_lastname,
            isDataValid = isFirstNameValid and isLastNameValid,
            firstName = firstName,
            lastName = lastName
        )
    }

    fun passwordFormDataChanged(password: String) {

        val isPasswordValid = isPasswordValid(password)

        _updatePasswordFormState.value = UpdateUserPasswordFormState(
            passwordError = if (isPasswordValid) null else R.string.profile_settings_invalid_password,
            isDataValid = isPasswordValid,
            password = password
        )
    }

    private fun isFirstNameValid(firstName: String): Boolean {
        return firstName.length >= Config.FIRSTNAME_MIN_LENGTH && firstName.length <= Config.FIRSTNAME_MAX_LENGTH
    }

    private fun isLastNameValid(lastName: String): Boolean {
        return lastName.length >= Config.LASTNAME_MIN_LENGTH && lastName.length <= Config.LASTNAME_MAX_LENGTH
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= Config.PASSWORD_MIN_LENGTH && password.length <= Config.PASSWORD_MAX_LENGTH
    }

    fun clearUpdateUsernamesResult() {
        _updateUsernamesResult.value = Result.ready()
    }

    fun clearUpdateUserPasswordResult() {
        _updatePasswordResult.value = Result.ready()
    }

    fun clearUploadUserImageResult() {
        _uploadUserImageResult.value = Result.ready()
    }
}