package com.project.beam.data.auth

import android.content.Context
import com.project.beam.data.core.ApiClient
import com.project.beam.data.core.TokenManager

class AuthRepository(private val context: Context) {

    suspend fun googleLogin(idToken: String): Result<TokenResponse> {
        return try {
            val response = ApiClient.authApi.googleLogin(GoogleLoginRequest(idToken))
            TokenManager.saveToken(context, response.access_token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<UserResponse> {
        return try {
            val user = ApiClient.authApi.getMe()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}