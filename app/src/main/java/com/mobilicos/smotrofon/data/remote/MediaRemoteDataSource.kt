package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.queries.RemoveVideoQuery
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.MediaService
import com.mobilicos.smotrofon.network.services.VideoService
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * fetches data from remote source
 */
class MediaRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun fetchVideoSuggestions(q: String): Result<SuggestionResponse> {
        val videoService = retrofit.create(VideoService::class.java)
        return getResponse(
            request = { videoService.getVideoSuggestionsListData(q) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun fetchRelatedMedia(mediaId: Int, type: Int = Config.TYPE_VIDEO): Result<MediaResponse> {
        val service = retrofit.create(VideoService::class.java)
        println("RESULT //// TYPE $type")
        return getResponse(
            request = { service.getRelatedMediaData(mediaId=mediaId, type=type) },
            defaultErrorMessage = "Error fetching Movie list")
    }
}