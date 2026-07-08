package com.project.beam.data.auth

data class GoogleLoginRequest(
    val id_token: String
)

data class TokenResponse(
    val access_token: String
)

data class UserResponse(
    val id: Int,
    val email: String,
    val name: String
)

data class SloganResponse(
    val slogan: String
)