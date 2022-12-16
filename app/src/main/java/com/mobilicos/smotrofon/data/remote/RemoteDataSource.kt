package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.ErrorUtils
import retrofit2.Response
import retrofit2.Retrofit

/**
 * fetches data from remote source
 */
open class RemoteDataSource constructor(private val retrofit: Retrofit) {
    suspend fun <T> getResponse(request: suspend () -> Response<T>, defaultErrorMessage: String): Result<T> {
        return try {
            println("I'm working in thread ${Thread.currentThread().name}")
            val result = request()
            println("RESULT:::::-> $result")
            if (result.isSuccessful) {
                return Result.success(result.body())
            } else {
                val errorResponse = ErrorUtils.parseError(result, retrofit)
                Result.error(errorResponse?.status_message ?: defaultErrorMessage, errorResponse)
            }
        } catch (e: Throwable) {
            println("RESULT // ERROR: ${e.localizedMessage}")
            Result.error("RESULT /// ERROR: ${e.localizedMessage}", null)
        }
    }
}