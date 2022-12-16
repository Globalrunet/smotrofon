package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API Service
 */
interface LessonService {
    @GET("api/lessons/list/")
    suspend fun getLessonsListData(
        @Query("p") page: Int,
        @Query("pj") project: Int = 0,
        @Query("t") type: Int = 0,
        @Query("q") query: String = "",
        @Query("s") size: Int = 10,
        @Query("lg") language: String = Config.DEFAULT_LANGUAGE
    ): Response<LessonsListResponse>
}