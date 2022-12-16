package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UpdateVideoDataResponse
import com.mobilicos.smotrofon.data.models.VideoResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service
 */
interface VideoService {
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

    @GET("video_ajax/related_media_list")
    suspend fun getRelatedMediaData(
        @Query("mid") mediaId: Int,
        @Query("p") page: Int = 1,
        @Query("t") type: Int = Config.TYPE_VIDEO,
        @Query("s") size: Int = 10
    ): Response<MediaResponse>

    @FormUrlEncoded
    @POST("api/user/video/edit/")
    suspend fun updateVideoData(
        @Field("k") key: String,
        @Field("title") title: String,
        @Field("text") text: String,
        @Field("id") id: Int
    ): Response<UpdateVideoDataResponse>
}