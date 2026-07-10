package com.project.beam.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.data.emotion.EmotionCard
import kotlinx.coroutines.delay

@Composable
fun EmotionCelebrationOverlay(
    category: String,
    isDark: Boolean,
    style: EmotionCard?,
    onFinished: () -> Unit
) {
    val bgColor = when (category) {
        "행복" -> if (isDark) Color(0xFF4A3A00) else Color(0xFFFFF3C4)
        "우울" -> if (isDark) Color(0xFF1A2456) else Color(0xFFDAE0F5)
        "외로움" -> if (isDark) Color(0xFF2D1F5E) else Color(0xFFE8DCFF)
        "짜증" -> if (isDark) Color(0xFF5C1A1A) else Color(0xFFFFD6D6)
        "슬픔" -> if (isDark) Color(0xFF0A3040) else Color(0xFFD6F0FF)
        else -> if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    }

    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 등장
        alpha.animateTo(1f, tween(300))
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        delay(200)
        textAlpha.animateTo(1f, tween(300))

        // 유지
        delay(1200)

        // 퇴장
        overlayAlpha.animateTo(0f, tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie 애니메이션
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                val lottieRes = style?.lottieRes
                if (lottieRes != null && lottieRes != 0) {
                    LottieIcon(
                        resId = lottieRes,
                        size = 200.dp,
                        isPlaying = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 텍스트
            Column(
                modifier = Modifier.alpha(textAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "감정을 담았어요",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "오늘의 $category, 여기 잘 보관할게요 🫙",
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
                )
            }
        }
    }
}