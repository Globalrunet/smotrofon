package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.data.models.SuggestionResponse
import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.data.queries.UpdateUserPasswordQuery
import com.mobilicos.smotrofon.data.queries.UpdateUsernamesQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.data.queries.UploadUserImageQuery
import com.mobilicos.smotrofon.data.remote.MediaRemoteDataSource
import com.mobilicos.smotrofon.data.remote.UserRemoteDataSource
import com.mobilicos.smotrofon.data.responses.UpdateUserPasswordResponse
import com.mobilicos.smotrofon.data.responses.UpdateUsernamesResponse
import com.mobilicos.smotrofon.data.responses.UploadUserImageResponse
import com.mobilicos.smotrofon.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class UserRepository @Inject constructor(
        private val userRemoteDataSource: UserRemoteDataSource
    ) {

    suspend fun fetchUserLogin(username: String, password: String): Flow<Result<UserLogin>> {
        return flow {
            emit(Result.loading())
            val result = userRemoteDataSource.fetchLogin(username, password)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun fetchUserRegistration(firstname: String, lastname: String, email: String, password: String): Flow<Result<UserLogin>> {
        return flow {
            emit(Result.loading())
            val result = userRemoteDataSource.fetchRegistration(firstname = firstname, lastname = lastname, email = email, password = password)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateUsernames(query: UpdateUsernamesQuery): Flow<Result<UpdateUsernamesResponse>> {
        return flow {
            emit(Result.loading())
            val result = userRemoteDataSource.updateUsernames(query = query)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePassword(query: UpdateUserPasswordQuery): Flow<Result<UpdateUserPasswordResponse>> {
        return flow {
            emit(Result.loading())
            val result = userRemoteDataSource.updatePassword(query = query)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun uploadUserImage(q: UploadUserImageQuery): Flow<Result<UploadUserImageResponse>> {
        return flow {
            emit(Result.loading())
            val result = userRemoteDataSource.uploadUserImage(q)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}