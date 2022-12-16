package com.mobilicos.smotrofon.model

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)