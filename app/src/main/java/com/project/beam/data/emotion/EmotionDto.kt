package com.project.beam.data.emotion

import androidx.compose.ui.graphics.Color

// UI 모델
data class EmotionCard(
    val emoji: String,
    val name: String,
    val count: Int,
    val lightColor: Color,
    val darkColor: Color,
    val lightGlow: Color,
    val darkGlow: Color
)

// 요청
data class RecordCreateRequest(
    val content: String
)

// 응답
data class RecordResponse(
    val id: Int,
    val content: String,
    val category: String,
    val created_at: String
)

// 월별 통계
data class EmotionStat(
    val category: String,
    val count: Int
)

data class MonthlyEmotionResponse(
    val month: String,
    val emotions: List<EmotionStat>
)