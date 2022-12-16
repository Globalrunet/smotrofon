package com.mobilicos.smotrofon.data.formstate

import com.mobilicos.smotrofon.model.Error
import com.mobilicos.smotrofon.model.Result

/**
 * Data validation state of the login form.
 */
data class UpdateUsernamesFormState(
    var isInited: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val isDataValid: Boolean = false,
    val firstNameError: Int? = null,
    val lastNameError: Int? = null
)

