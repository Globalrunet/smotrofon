package com.mobilicos.smotrofon.data.formstate

import com.mobilicos.smotrofon.model.Error
import com.mobilicos.smotrofon.model.Result

/**
 * Data validation state of the login form.
 */
data class UpdateAudioFormState(
    val id: Int = 0,
    val key: String = "",
    val title: String = "",
    val text: String = "",
    val image: String = "",
    val isDataValid: Boolean = false,
    val titleError: Int? = null,
    val textError: Int? = null,
    val imageError: Int? = null
)

