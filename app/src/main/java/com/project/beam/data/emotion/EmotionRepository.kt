package com.project.beam.data.emotion

import com.project.beam.data.core.ApiClient

class EmotionRepository {

    suspend fun createRecord(content: String): Result<RecordResponse> {
        return try {
            val response = ApiClient.emotionApi.createRecord(RecordCreateRequest(content))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyRecords(): Result<List<RecordResponse>> {
        return try {
            val records = ApiClient.emotionApi.getMyRecords()
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyStats(): Result<List<MonthlyEmotionResponse>> {
        return try {
            val stats = ApiClient.emotionApi.getMonthlyStats()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(recordId: Int): Result<Unit> {
        return try {
            ApiClient.emotionApi.deleteRecord(recordId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyReport(): Result<MonthlyReportResponse> {
        return try {
            val report = ApiClient.emotionApi.getMonthlyReport()
            android.util.Log.d("MonthlyReport", "성공: $report")
            Result.success(report)
        } catch (e: Exception) {
            android.util.Log.e("MonthlyReport", "실패: ${e.message}")
            Result.failure(e)
        }
    }
}