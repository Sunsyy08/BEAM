package com.project.beam.data.emotion

import retrofit2.http.*

interface EmotionApi {
    @POST("records")
    suspend fun createRecord(@Body request: RecordCreateRequest): RecordResponse

    @GET("records")
    suspend fun getMyRecords(): List<RecordResponse>

    @GET("records/{record_id}")
    suspend fun getRecord(@Path("record_id") recordId: Int): RecordResponse

    @GET("records/stats/monthly")
    suspend fun getMonthlyStats(): List<MonthlyEmotionResponse>

    @DELETE("records/{record_id}")
    suspend fun deleteRecord(@Path("record_id") recordId: Int)

    @POST("records/stats/monthly-report")
    suspend fun getMonthlyReport(): MonthlyReportResponse
}