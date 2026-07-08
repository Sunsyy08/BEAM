package com.project.beam.data.slogan

import com.project.beam.data.auth.SloganResponse
import retrofit2.http.GET

interface SloganApi {
    @GET("slogan")
    suspend fun getSlogan(): SloganResponse
}