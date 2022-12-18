package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.responses.CommentsListResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

/**
 * Retrofit API Service
 */
interface CommentService {
    @GET("api/comments/list/{app_label}/{model}/{object_id}/")
    suspend fun getCommentsListData(
        @Path("app_label") app_label: String,
        @Path("model") model: String,
        @Path("object_id") object_id: Int,
        @Query("p") page: Int,
        @Query("s") size: Int = 10
    ): Response<CommentsListResponse>
}