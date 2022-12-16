package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.*
import com.mobilicos.smotrofon.data.responses.UpdateUserPasswordResponse
import com.mobilicos.smotrofon.data.responses.UpdateUsernamesResponse
import com.mobilicos.smotrofon.data.responses.UploadUserImageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service
 */
interface UserService {
    @FormUrlEncoded
    @POST("user/ajax/login/")
    suspend fun loginUser(
        @Field("email") username: String,
        @Field("password") password: String
    ): Response<UserLogin>

    @FormUrlEncoded
    @POST("user/ajax/registration/")
    suspend fun registrationUser(
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<UserLogin>

    @FormUrlEncoded
    @POST("api/user/update/usernames/")
    suspend fun updateUsernames(
        @Field("k") key: String,
        @Field("first_name") firstname: String,
        @Field("last_name") lastname: String
    ): Response<UpdateUsernamesResponse>

    @FormUrlEncoded
    @POST("api/user/update/password/")
    suspend fun updatePassword(
        @Field("k") key: String,
        @Field("password") password: String
    ): Response<UpdateUserPasswordResponse>

    @Multipart
    @POST("api/user/upload/image/")
    suspend fun uploadUserImage(
        @Part("k") key: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<UploadUserImageResponse>
}