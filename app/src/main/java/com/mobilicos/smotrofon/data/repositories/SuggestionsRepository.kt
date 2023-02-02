package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.other.RemoteDataSource
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.other.ResultOld
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class SuggestionsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    suspend fun fetchVideoSuggestions(query: String): Flow<ResultOld<SuggestionResponse>?> {ResultOld
        return flow {
            emit(ResultOld.loading())
            val result = remoteDataSource.fetchVideoSuggestions(query=query)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}