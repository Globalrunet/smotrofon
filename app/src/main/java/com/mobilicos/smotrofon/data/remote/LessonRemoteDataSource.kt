package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.DownloadLessonService
import okhttp3.ResponseBody
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * fetches data from remote source
 */
class LessonRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun downloadLessonByIdent(ident: Int): Result<ResponseBody> {
        val downloadLessonService = retrofit.create(DownloadLessonService::class.java)
        val fullUrl = "${Config.BASE_URL_STATIC}/apps-data/lessons/zip/$ident.zip"
        return getResponse(
            request = { downloadLessonService.downloadLessonByIdent(url = fullUrl) },
            defaultErrorMessage = "Error fetching user login")
    }

    suspend fun downloadLessonByIdentStream(ident: Int): ResponseBody {
        val downloadLessonService = retrofit.create(DownloadLessonService::class.java)
        val fullUrl = "${Config.BASE_URL_STATIC}/apps-data/lessons/zip/$ident.zip"
        return downloadLessonService.downloadLessonByIdentStreaming(url = fullUrl)
    }
}