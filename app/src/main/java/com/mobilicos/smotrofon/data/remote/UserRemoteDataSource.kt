package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.models.User
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.data.queries.UpdateUserPasswordQuery
import com.mobilicos.smotrofon.data.queries.UpdateUsernamesQuery
import com.mobilicos.smotrofon.data.queries.UploadMediaPosterQuery
import com.mobilicos.smotrofon.data.queries.UploadUserImageQuery
import com.mobilicos.smotrofon.data.responses.SubscribeUserResponse
import com.mobilicos.smotrofon.data.responses.UpdateUserPasswordResponse
import com.mobilicos.smotrofon.data.responses.UpdateUsernamesResponse
import com.mobilicos.smotrofon.data.responses.UploadUserImageResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.MediaService
import com.mobilicos.smotrofon.network.services.UserService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

/**
 * fetches data from remote source
 */
class UserRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun fetchLogin(username: String, password: String): Result<UserLogin> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.loginUser(username=username, password=password) },
            defaultErrorMessage = "Error fetching user login")
    }

    suspend fun fetchRegistration(firstname: String, lastname: String, email: String, password: String): Result<UserLogin> {
        val userService = retrofit.create(UserService::class.java)
        println("RESULT /// ${firstname} ${lastname} ${email} ${password} ")
        return getResponse(
            request = { userService.registrationUser(firstname = firstname, lastname = lastname, email = email, password = password) },
            defaultErrorMessage = "Error fetching user login")
    }

    suspend fun updateUsernames(query: UpdateUsernamesQuery): Result<UpdateUsernamesResponse> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.updateUsernames(key = query.key, firstname = query.firstName, lastname = query.lastName) },
            defaultErrorMessage = "Error fetching user login")
    }

    suspend fun updatePassword(query: UpdateUserPasswordQuery): Result<UpdateUserPasswordResponse> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.updatePassword(key = query.key, password = query.password) },
            defaultErrorMessage = "Error fetching user login")
    }

    suspend fun uploadUserImage(q: UploadUserImageQuery): Result<UploadUserImageResponse> {
        val service = retrofit.create(UserService::class.java)

        val file = File(q.image)

        val keyPart: RequestBody = q.key
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val filePart = MultipartBody.Part.createFormData(
            "img",
            file.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return getResponse(
            request = { service.uploadUserImage(
                image = filePart,
                key = keyPart)},
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun subscribeUser(key: String, otherUserId: Int): Result<SubscribeUserResponse> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.subscribeUser(key = key, otherUserId = otherUserId) },
            defaultErrorMessage = "Error fetching subscribe user")
    }

    suspend fun getUserData(id: Int): Result<User> {
        val userService = retrofit.create(UserService::class.java)
        return getResponse(
            request = { userService.getUserData(id = id) },
            defaultErrorMessage = "Error fetching subscribe user")
    }
}