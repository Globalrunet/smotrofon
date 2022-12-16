package com.mobilicos.smotrofon.data

import com.mobilicos.smotrofon.data.models.SuggestionResponse
import retrofit2.Response
import retrofit2.Retrofit

class RemoteDataSource constructor(private val retrofit: Retrofit) {
    suspend fun fetchAudioSuggestions(query:String): Result<SuggestionResponse> {
        val service = retrofit.create(GetSuggestions::class.java)
        return getResponse(
            request = { service.getAudioListData(query=query) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun fetchVideoSuggestions(query:String): Result<SuggestionResponse> {
        val service = retrofit.create(GetSuggestions::class.java)
        return getResponse(
            request = { service.getVideoListData(query=query) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    private suspend fun <T> getResponse(request: suspend () -> Response<T>, defaultErrorMessage: String): Result<T> {
        return try {
            val result = request.invoke()
            if (result.isSuccessful) {
                return Result.success(result.body())
            } else {
//                val errorResponse = RetrofitClient.ErrorUtils.parseError(result, retrofit)
                Result.error(defaultErrorMessage, null)
            }
        } catch (e: Throwable) {
            Result.error("Unknown Error", null)
        }
    }
}