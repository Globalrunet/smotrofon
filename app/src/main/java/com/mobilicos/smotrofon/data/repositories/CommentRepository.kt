package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.remote.CommentRemoteDataSource
import com.mobilicos.smotrofon.data.remote.MediaRemoteDataSource
import com.mobilicos.smotrofon.data.remote.VideoRemoteDataSource
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
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
class CommentRepository @Inject constructor(
        private val commentRemoteDataSource: CommentRemoteDataSource
    ) {

    suspend fun addCommentData(q: CommentsAddQuery): Flow<Result<CommentAddResponse>> {
        return flow {
            emit(Result.loading())
            val result = commentRemoteDataSource.addCommentData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}