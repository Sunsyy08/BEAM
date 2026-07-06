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

// 요청 데이터
data class EmotionRequest(
    val name: String,
    val emoji: String,
    val content: String
)

// 응답 데이터
data class EmotionResponse(
    val id: Int,
    val name: String,
    val emoji: String,
    val count: Int
)

data class RecentRecordResponse(
    val id: Int,
    val content: String,
    val emotion_name: String,
    val emotion_emoji: String,
    val created_at: String
)