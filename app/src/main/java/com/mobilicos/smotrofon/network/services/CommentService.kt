package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.responses.CommentEditResponse
import com.mobilicos.smotrofon.data.responses.CommentRemoveResponse
import com.mobilicos.smotrofon.data.responses.CommentsListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @POST("api/comments/add/{app_label}/{model}/{object_id}/")
    suspend fun addCommentDataOld(
        @Path("app_label") app_label: String,
        @Path("model") model: String,
        @Path("object_id") object_id: Int,
        @Part("k") key: String,
        @Part("parent_id") parent_id: Int = 0,
        @Part("text") text: String,
        @Part image: MultipartBody.Part? = null
    ): Response<CommentAddResponse>

    @Multipart
    @POST("api/comments/add/{app_label}/{model}/{object_id}/")
    suspend fun addCommentData(
        @Path("app_label") app_label: String,
        @Path("model") model: String,
        @Path("object_id") object_id: Int,
        @Part("k") key: RequestBody,
        @Part("parent_id") parent_id: RequestBody,
        @Part("text") text: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<CommentAddResponse>

    @FormUrlEncoded
    @POST("api/comments/remove/{comment_id}/")
    suspend fun removeCommentData(
        @Path("comment_id") comment_id: Int,
        @Field("k") key: String
    ): Response<CommentRemoveResponse>

    @FormUrlEncoded
    @POST("api/comments/edit/{comment_id}/")
    suspend fun editCommentData(
        @Path("comment_id") comment_id: Int,
        @Field("k") key: String,
        @Field("text") text: String
    ): Response<CommentEditResponse>
}