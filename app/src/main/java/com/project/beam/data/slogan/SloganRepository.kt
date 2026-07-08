package com.project.beam.data.slogan

import android.content.Context
import com.project.beam.data.core.ApiClient
import com.project.beam.data.core.TokenManager
import java.time.LocalDate

class SloganRepository(private val context: Context) {

    suspend fun getTodaySlogan(): Result<String> {
        return try {
            val today = LocalDate.now().toString()
            val (savedSlogan, savedDate) = TokenManager.getSloganData(context)

            // 오늘 이미 받았으면 캐시에서 반환
            if (savedSlogan != null && savedDate == today) {
                return Result.success(savedSlogan)
            }

            // 새로 호출
            val response = ApiClient.sloganApi.getSlogan()
            TokenManager.saveSlogan(context, response.slogan, today)
            Result.success(response.slogan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}