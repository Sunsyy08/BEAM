package com.project.beam.data.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): TokenResponse

    @GET("auth/user")
    suspend fun getMe(): UserResponse
}