package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.responses.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

/**
 * Retrofit API Service
 */
interface ReportService {
    @FormUrlEncoded
    @POST("api/report/add/{app_label}/{model}/{object_id}/")
    suspend fun addReportData(
        @Path("app_label") app_label: String,
        @Path("model") model: String,
        @Path("object_id") object_id: Int,
        @Field("k") key: String,
        @Field("text") text: String
    ): Response<ReportAddResponse>
}