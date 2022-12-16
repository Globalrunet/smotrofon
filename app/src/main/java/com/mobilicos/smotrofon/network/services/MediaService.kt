package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.data.responses.RemoveVideoResponse
import com.mobilicos.smotrofon.data.responses.UploadVideoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query


/**
 * Retrofit API Service
 */
interface MediaService {
    @GET("video_ajax/api_video_list")
    suspend fun getHiltVideoListData(
        @Query("p") page: Int,
        @Query("t") type: Int = Config.TYPE_VIDEO,
        @Query("q") query: String = "",
        @Query("s") size: Int = 10
    ): Response<MediaResponse>

    @GET("suggestions/video/")
    suspend fun getVideoSuggestionsListData(
        @Query("q") query: String,
        @Query("s") size: Int = 10
    ): Response<SuggestionResponse>

    @GET("video_ajax/api_video_list")
    suspend fun getVideoListData(
        @Query("p") page: Int,
        @Query("t") type: Int = 1,
        @Query("s") size: Int = 10
    ): VideoResponse

    @GET("video_ajax/api_user_media_list")
    suspend fun getUserMediaListData(
        @Query("p") page: Int,
        @Query("t") type: Int = Config.TYPE_VIDEO,
        @Query("u") user: Int = 0,
        @Query("s") size: Int = 10
    ): Response<MediaResponse>

    @GET("video_ajax/api_profile_media_list")
    suspend fun getProfileMediaListData(
        @Query("p") page: Int,
        @Query("t") type: Int = Config.TYPE_VIDEO,
        @Query("k") key: String,
        @Query("q") query: String = "",
        @Query("o") sort: Int = 0,
        @Query("s") size: Int = 10
    ): Response<MediaResponse>

    @GET("video_ajax/api_channels/")
    suspend fun getChannelsListData(
        @Query("p") page: Int,
        @Query("q") query: String = "",
        @Query("s") size: Int = 10
    ): Response<ChannelsResponse>

    @FormUrlEncoded
    @POST("api/user/media/edit/")
    suspend fun updateMediaData(
        @Field("t") type: Int,
        @Field("k") key: String,
        @Field("title") title: String,
        @Field("text") text: String,
        @Field("id") size: Int
    ): Response<MediaResponse>

    @Multipart
    @POST("api/user/media/upload/poster/")
    suspend fun uploadMediaPoster(
        @Part("t") type: RequestBody,
        @Part("k") key: RequestBody,
        @Part("id") id: RequestBody,
        @Part image:MultipartBody.Part
    ): Response<UploadMediaPosterResponse>

    @FormUrlEncoded
    @POST("api/user/media/remove/")
    suspend fun removeMedia(
        @Field("t") type: Int,
        @Field("k") key: String,
        @Field("id") id: Int
    ): Response<RemoveMediaResponse>

    @Multipart
    @POST("api/user/media/upload/video/")
    suspend fun uploadVideo(
        @Part("k") key: RequestBody,
        @Part("id") id: RequestBody,
        @Part("is_end") is_end: RequestBody,
        @Part("next_chunk") next_chunk: RequestBody,
        @Part("filename") filename: RequestBody,
        @Part("size") size: RequestBody,
        @Part("uploaded_bytes") uploaded_bytes: RequestBody,
        @Part file:MultipartBody.Part
    ): Response<UploadVideoResponse>
}