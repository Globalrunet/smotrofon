package com.mobilicos.smotrofon.data.other

import com.mobilicos.smotrofon.data.models.RelatedAudioListResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GetRelatedAudiosApi {

    @GET("video_ajax/api_audio_list")
    suspend fun getAudiosData(
        @Query("vid") video: Int,
        @Query("s") size: Int = 10
    ): RelatedAudioListResponse

    companion object {

        private const val BASE_URL = "http://smotrofon.ru/"

        operator fun invoke(): GetRelatedAudiosApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().also { client ->
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                client.addInterceptor(logging)
            }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetRelatedAudiosApi::class.java)
    }
}

