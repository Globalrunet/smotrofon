package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.DownloadLessonService
import com.mobilicos.smotrofon.network.services.UserService
import com.mobilicos.smotrofon.network.services.VideoService
import okhttp3.ResponseBody
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * fetches data from remote source
 */
class LessonRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun downloadLessonByIdent(ident: Int): Result<ResponseBody> {
        val downloadLessonService = retrofit.create(DownloadLessonService::class.java)
        return getResponse(
            request = { downloadLessonService.downloadLessonByIdent(ident=ident) },
            defaultErrorMessage = "Error fetching user login")
    }
}