package com.mobilicos.smotrofon.model

/**
 * Data validation state of the login form.
 */
data class RegistrationFormState(
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val password: String = "",
    val firstnameError: Int? = null,
    val lastnameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)