package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.responses.CommentEditResponse
import com.mobilicos.smotrofon.data.responses.CommentRemoveResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.CommentService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

class CommentRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun addCommentData(q: CommentsAddQuery): Result<CommentAddResponse> {
        val service = retrofit.create(CommentService::class.java)

        var imagePart: MultipartBody.Part? = null
        q.images?.let {
            if (it.isNotEmpty()) {
                val file = File(it[0])

                imagePart = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }
        }


        val keyPart: RequestBody = q.key.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val parentIdPart: RequestBody = q.parent_id.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val textPart: RequestBody = q.text.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        return getResponse(
            request = { service.addCommentData(app_label = q.app_label,
                model = q.model,
                object_id = q.object_id,
                key = keyPart,
                parent_id = parentIdPart,
                text = textPart,
                image = imagePart) },
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