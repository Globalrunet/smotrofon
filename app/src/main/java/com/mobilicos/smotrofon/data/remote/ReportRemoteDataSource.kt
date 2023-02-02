package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.responses.CommentAddResponse
import com.mobilicos.smotrofon.data.responses.CommentEditResponse
import com.mobilicos.smotrofon.data.responses.CommentRemoveResponse
import com.mobilicos.smotrofon.data.responses.ReportAddResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.CommentService
import com.mobilicos.smotrofon.network.services.ReportService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

class ReportRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun addReportData(q: ReportAddQuery): Result<ReportAddResponse> {
        val service = retrofit.create(ReportService::class.java)

        return getResponse(
            request = { service.addReportData(app_label = q.app_label,
                model = q.model,
                object_id = q.object_id,
                key = q.key,
                text = q.text) },
            defaultErrorMessage = "Error fetching Movie list")
    }
}