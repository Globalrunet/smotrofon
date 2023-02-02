package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UpdateVideoDataResponse
import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.remote.AudioRemoteDataSource
import com.mobilicos.smotrofon.data.responses.*
import com.mobilicos.smotrofon.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class AudioRepository @Inject constructor(
        private val audioRemoteDataSource: AudioRemoteDataSource
    ) {

    suspend fun fetchVideosSuggestions(q: String): Flow<Result<SuggestionResponse>?> {
        return flow {
            emit(Result.loading())
            val result = audioRemoteDataSource.fetchAudioSuggestions(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateAudioData(q: EditAudioQuery): Flow<Result<UpdateAudioDataResponse>> {
        return flow {
            emit(Result.loading())
            val result = audioRemoteDataSource.updateAudioData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadAudioPoster(q: UploadAudioPosterQuery): Flow<Result<UploadAudioPosterResponse>> {
        return flow {
            emit(Result.loading())
            val result = audioRemoteDataSource.uploadAudioPoster(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeAudio(q: RemoveAudioQuery): Flow<Result<RemoveAudioResponse>> {
        return flow {
            emit(Result.loading())
            val result = audioRemoteDataSource.removeAudioData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadAudio(q: UploadAudioQuery): Flow<Result<UploadAudioResponse>> {
        return flow {
            emit(Result.loading())
            val result = audioRemoteDataSource.uploadAudio(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}