package com.mobilicos.smotrofon.ui.user.login

import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.repositories.UserRepository
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.model.LoginFormState
import com.mobilicos.smotrofon.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    private val _loginResult = MutableStateFlow<Result<UserLogin>>(Result.ready())
    val loginResult: StateFlow<Result<UserLogin>> = _loginResult


    fun login(username: String, password: String) {
        viewModelScope.launch {
            userRepository.fetchUserLogin(username=username, password=password).collect() {
                _loginResult.value = it
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {

        println("RESULT ON U P : $username / $password")

        val isUserNameValid = isUserNameValid(username)
        val isPasswordValid = isPasswordValid(password)

        _loginFormState.value = LoginFormState(
            usernameError = if (isUserNameValid) null else R.string.invalid_email,
            passwordError = if (isPasswordValid) null else R.string.invalid_password,
            isDataValid = isUserNameValid and isPasswordValid,
            username = username,
            password = password
        )
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}