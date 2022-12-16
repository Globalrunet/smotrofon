package com.mobilicos.smotrofon.data

import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UpdateVideoDataResponse
import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.remote.MediaRemoteDataSource
import com.mobilicos.smotrofon.data.remote.VideoRemoteDataSource
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.data.responses.RemoveVideoResponse
import com.mobilicos.smotrofon.data.responses.UploadVideoResponse
import com.mobilicos.smotrofon.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class VideoRepository @Inject constructor(
        private val videoRemoteDataSource: VideoRemoteDataSource
    ) {

    suspend fun updateVideoData(q: EditVideoQuery): Flow<Result<UpdateVideoDataResponse>> {
        return flow {
            emit(Result.loading())
            val result = videoRemoteDataSource.updateVideoData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadVideoPoster(q: UploadMediaPosterQuery): Flow<Result<UploadMediaPosterResponse>> {
        return flow {
            emit(Result.loading())
            val result = videoRemoteDataSource.uploadVideoPoster(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeVideo(q: RemoveMediaQuery): Flow<Result<RemoveMediaResponse>> {
        return flow {
            emit(Result.loading())
            val result = videoRemoteDataSource.removeVideoData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadVideo(q: UploadVideoQuery): Flow<Result<UploadVideoResponse>> {
        return flow {
            emit(Result.loading())
            val result = videoRemoteDataSource.uploadVideo(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}