package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.ChannelsResponse
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.responses.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service
 */
interface AudioService {

    @GET("suggestions/audio/")
    suspend fun getAudioSuggestionsListData(
        @Query("q") query: String,
        @Query("s") size: Int = 10
    ): Response<SuggestionResponse>

    @GET("audio_ajax/api_profile_audio_list")
    suspend fun getProfileAudioListData(
        @Query("p") page: Int,
        @Query("t") type: Int = Config.TYPE_VIDEO,
        @Query("k") key: String,
        @Query("q") query: String = "",
        @Query("o") sort: Int = 0,
        @Query("s") size: Int = 10
    ): Response<AudioResponse>

    @FormUrlEncoded
    @POST("api/user/audio/edit/")
    suspend fun updateAudioData(
        @Field("k") key: String,
        @Field("title") title: String,
        @Field("text") text: String,
        @Field("id") id: Int
    ): Response<UpdateAudioDataResponse>

    @Multipart
    @POST("api/user/audio/upload/poster/")
    suspend fun uploadAudioPoster(
        @Part("t") type: RequestBody,
        @Part("k") key: RequestBody,
        @Part("id") id: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<UploadAudioPosterResponse>

    @FormUrlEncoded
    @POST("api/user/audio/remove/")
    suspend fun removeAudio(
        @Field("t") type: Int,
        @Field("k") key: String,
        @Field("id") id: Int
    ): Response<RemoveAudioResponse>

    @Multipart
    @POST("api/user/audio/upload/video/")
    suspend fun uploadAudio(
        @Part("k") key: RequestBody,
        @Part("id") id: RequestBody,
        @Part("is_end") is_end: RequestBody,
        @Part("next_chunk") next_chunk: RequestBody,
        @Part("filename") filename: RequestBody,
        @Part("size") size: RequestBody,
        @Part("uploaded_bytes") uploaded_bytes: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadAudioResponse>

}