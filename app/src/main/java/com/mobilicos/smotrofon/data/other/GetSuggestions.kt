package com.mobilicos.smotrofon.data.other

import com.mobilicos.smotrofon.data.models.SuggestionResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GetSuggestions {

    @GET("suggestions/audio/")
    suspend fun getAudioListData(
        @Query("q") query: String,
        @Query("s") size: Int = 10
    ): Response<SuggestionResponse>

    @GET("suggestions/video/")
    suspend fun getVideoListData(
        @Query("q") query: String,
        @Query("s") size: Int = 10
    ): Response<SuggestionResponse>

    companion object {

        private const val BASE_URL = "http://smotrofon.ru/"

        operator fun invoke(): GetSuggestions {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().also { client ->
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                }.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GetSuggestions::class.java)
        }
    }
}

