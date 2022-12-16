package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit API Service
 */
interface CourseService {
    @GET("api/courses/lessons/list/")
    suspend fun getLessonsListData(
        @Query("p") page: Int,
        @Query("t") type: Int = 0,
        @Query("q") query: String = "",
        @Query("s") size: Int = 10,
        @Query("lg") language: String = Config.DEFAULT_LANGUAGE
    ): Response<CoursesLessonsListResponse>

    @GET("/apps-data/courses/zip/c_{lesson_ident}.zip")
    @Streaming
    suspend fun downloadCoursesLessonByIdent(
        @Path("lesson_ident") ident: Int
    ): Response<ResponseBody>
}