package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.remote.CommentRemoteDataSource
import com.mobilicos.smotrofon.data.remote.MediaRemoteDataSource
import com.mobilicos.smotrofon.data.remote.ReportRemoteDataSource
import com.mobilicos.smotrofon.data.remote.VideoRemoteDataSource
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
class ReportRepository @Inject constructor(
    private val reportRemoteDataSource: ReportRemoteDataSource) {

    suspend fun addReportData(q: ReportAddQuery): Flow<Result<ReportAddResponse>> {
        return flow {
            emit(Result.loading())
            val result = reportRemoteDataSource.addReportData(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}