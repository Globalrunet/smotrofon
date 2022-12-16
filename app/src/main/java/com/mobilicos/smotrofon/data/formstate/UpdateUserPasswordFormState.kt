package com.mobilicos.smotrofon.data.formstate

import com.mobilicos.smotrofon.model.Error
import com.mobilicos.smotrofon.model.Result

/**
 * Data validation state of the login form.
 */
data class UpdateUserPasswordFormState(
    val password: String = "",
    val isDataValid: Boolean = false,
    val passwordError: Int? = null
)

