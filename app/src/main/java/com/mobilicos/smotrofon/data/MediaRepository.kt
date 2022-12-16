package com.mobilicos.smotrofon.data

import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.remote.MediaRemoteDataSource
import com.mobilicos.smotrofon.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class MediaRepository @Inject constructor(
        private val mediaRemoteDataSource: MediaRemoteDataSource
    ) {

    suspend fun fetchVideosSuggestions(q: String): Flow<Result<SuggestionResponse>?> {
        return flow {
            emit(Result.loading())
            val result = mediaRemoteDataSource.fetchVideoSuggestions(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun fetchRelatedMedia(mediaId: Int, type: Int): Flow<Result<MediaResponse>?> {
        return flow {
            emit(Result.loading())
            val result = mediaRemoteDataSource.fetchRelatedMedia(mediaId=mediaId, type=type)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun fetchUpdateMediaData(mediaId: Int, type: Int): Flow<Result<MediaResponse>?> {
        return flow {
            emit(Result.loading())
            val result = mediaRemoteDataSource.fetchRelatedMedia(mediaId=mediaId, type=type)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}