package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.data.models.VideoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service
 */
interface DownloadLessonService {
    @GET("/apps-data/lessons/zip/{lesson_ident}.zip")
    @Streaming
    suspend fun downloadLessonByIdent(
        @Path("lesson_ident") ident: Int
    ): Response<ResponseBody>
}