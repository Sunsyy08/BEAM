package com.project.beam.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.project.beam.MainActivity
import com.project.beam.R
import com.project.beam.data.core.ApiClient
import com.project.beam.data.core.AuthInterceptor
import com.project.beam.data.core.TokenManager
import java.time.LocalDate

class BeamWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val token = TokenManager.getToken(context)
        AuthInterceptor.token = token

        var topEmotion: String? = null
        var topCount: Int = 0
        var hasRecord: Boolean = false

        if (token != null) {
            try {
                val records = ApiClient.emotionApi.getMyRecords()
                val today = LocalDate.now().toString()
                val todayRecords = records.filter { it.created_at.take(10) == today }

                if (todayRecords.isNotEmpty()) {
                    hasRecord = true
                    val grouped = todayRecords.groupBy { it.category }
                    val top = grouped.maxByOrNull { it.value.size }
                    topEmotion = top?.key
                    topCount = top?.value?.size ?: 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        provideContent {
            BeamWidgetContent(
                hasRecord = hasRecord,
                topEmotion = topEmotion,
                topCount = topCount
            )
        }
    }
}

@Composable
fun BeamWidgetContent(
    hasRecord: Boolean,
    topEmotion: String?,
    topCount: Int
) {
    val bgColor = ColorProvider(Color(0xFFFFFFFF))
    val textPrimary = ColorProvider(Color(0xFF1A1A1A))
    val textSecondary = ColorProvider(Color(0xFF888888))
    val accentColor = ColorProvider(Color(0xFF7C4DFF))

    val imageRes = when (topEmotion) {
        "행복" -> R.drawable.img_happy
        "우울" -> R.drawable.img_depression
        "슬픔" -> R.drawable.img_sad
        "짜증" -> R.drawable.img_annoying
        "외로움" -> R.drawable.img_loneliness
        else -> R.drawable.img_hello
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BEAM 텍스트
            Text(
                text = "BEAM",
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(6.dp))

            // 감정 이미지
            Image(
                provider = ImageProvider(imageRes),
                contentDescription = topEmotion ?: "hello",
                modifier = GlanceModifier
                    .size(64.dp)
            )

            Spacer(GlanceModifier.height(8.dp))

            if (!hasRecord) {
                // ── 기록 없을 때 ──
                Text(
                    text = "오늘의 감정",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "아직 안 담았어요",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "탭해서 기록하기 →",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                // ── 기록 있을 때 ──
                Text(
                    text = "오늘의 감정",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = topEmotion ?: "",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "오늘 ${topCount}번",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}