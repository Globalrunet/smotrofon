package com.mobilicos.smotrofon.network.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service
 */
interface DownloadLessonService {
    @GET
    @Streaming
    suspend fun downloadLessonByIdent(
        @Url url: String
    ): Response<ResponseBody>

    @GET
    @Streaming
    suspend fun downloadLessonByIdentStreaming(
        @Url url: String
    ): ResponseBody
}