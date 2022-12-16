package com.mobilicos.smotrofon.ui.user.registration

import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.repositories.UserRepository
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.model.RegistrationFormState
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registrationFormState = MutableStateFlow(RegistrationFormState())
    val registrationFormState: StateFlow<RegistrationFormState> = _registrationFormState

    private val _registrationResult = MutableStateFlow<Result<UserLogin>>(Result.ready())
    val registrationResult: StateFlow<Result<UserLogin>> = _registrationResult


    fun registration(firstname: String, lastname: String, email: String, password: String) {
        viewModelScope.launch {
            userRepository.fetchUserRegistration(firstname = firstname,  lastname = lastname, email = email, password = password).collect() {
                _registrationResult.value = it
            }
        }
    }

    fun registrationDataChanged(firstname: String, lastname: String, email: String, password: String) {

        val isFirstnameValid = isFirstnameValid(firstname)
        val isLastnameValid = isLastnameValid(lastname)
        val isEmailValid = isEmailValid(email)
        val isPasswordValid = isPasswordValid(password)

        _registrationFormState.value = RegistrationFormState(
            firstnameError = if (isFirstnameValid) null else R.string.invalid_email,
            lastnameError = if (isLastnameValid) null else R.string.login_error,
            emailError = if (isEmailValid) null else R.string.login_error,
            passwordError = if (isPasswordValid) null else R.string.invalid_password,
            isDataValid = isFirstnameValid and isLastnameValid and isEmailValid and isPasswordValid,
            firstname = firstname,
            lastname = lastname,
            email = email,
            password = password
        )
    }

    private fun isFirstnameValid(firstname: String): Boolean {
        return firstname.isNotEmpty()
    }

    private fun isLastnameValid(lastname: String): Boolean {
        return lastname.isNotEmpty()
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}