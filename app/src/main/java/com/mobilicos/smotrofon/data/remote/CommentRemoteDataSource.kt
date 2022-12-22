package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.responses.CommentEditResponse
import com.mobilicos.smotrofon.data.responses.CommentRemoveResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.CommentService
import retrofit2.Retrofit
import javax.inject.Inject

class CommentRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun addCommentData(q: CommentsAddQuery): Result<CommentAddResponse> {
        val service = retrofit.create(CommentService::class.java)
        return getResponse(
            request = { service.addCommentData(app_label = q.app_label,
                model = q.model,
                object_id = q.object_id,
                key = q.key,
                parent_id = q.parent_id,
                text = q.text) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun removeCommentData(q: CommentsRemoveQuery): Result<CommentRemoveResponse> {
        val service = retrofit.create(CommentService::class.java)
        return getResponse(
            request = { service.removeCommentData(comment_id = q.comment_id, key = q.key) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun editCommentData(q: CommentsEditQuery): Result<CommentEditResponse> {
        val service = retrofit.create(CommentService::class.java)
        return getResponse(
            request = { service.editCommentData(comment_id = q.comment_id,
                key = q.key,
                text = q.text) },
            defaultErrorMessage = "Error fetching Movie list")
    }
}