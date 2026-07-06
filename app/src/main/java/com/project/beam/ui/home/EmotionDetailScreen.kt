package com.project.beam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.data.emotion.EmotionCard
import com.project.beam.data.emotion.EmotionRecordResponse
import com.project.beam.ui.theme.*

// ── 샘플 데이터 ──────────────────────────────
val sampleEmotionRecords = listOf(
    EmotionRecordResponse(1, "정말 고마웠어. 네가 없었다면 버티지 못했을 거야.", "행복", "☀️", "2026.07.04"),
    EmotionRecordResponse(2, "처음으로 혼자 영화를 봤다. 생각보다 괜찮았다.", "행복", "☀️", "2026.06.27"),
    EmotionRecordResponse(3, "친구랑 오래 웃었더니 기분이 한결 나아졌다.", "행복", "☀️", "2026.06.21"),
    EmotionRecordResponse(4, "오늘은 진짜 행복했다. 이유는 모르겠지만.", "행복", "☀️", "2026.06.11")
)

// ── EmotionDetailScreen ───────────────────────
@Composable
fun EmotionDetailScreen(
    emotion: EmotionCard,
    records: List<EmotionRecordResponse> = sampleEmotionRecords,
    isDark: Boolean = isSystemInDarkTheme(),
    onBackClick: () -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface
    val tagColor = if (isDark) emotion.darkColor else emotion.lightColor

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            HomeBottomBar(
                isDark = isDark,
                bgColor = bgColor,
                textColor = textColor,
                subTextColor = subTextColor,
                onAddClick = {},
                onArchiveClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── 상단 바 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEAM",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDark) "🌙" else "☀️",
                        fontSize = 14.sp
                    )
                }
            }

            // ── 뒤로가기 + 타이틀 ──
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DarkSurface else Color(0xFFEEEEEE))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "뒤로가기",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "감정 기록",
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = emotion.emoji,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = emotion.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            // ── 기록 리스트 ──
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { record ->
                    EmotionRecordItem(
                        record = record,
                        isDark = isDark,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        tagColor = tagColor
                    )
                }
            }
        }
    }
}

// ── 기록 아이템 카드 ──────────────────────────
@Composable
fun EmotionRecordItem(
    record: EmotionRecordResponse,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    tagColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(16.dp)
    ) {
        Text(
            text = record.content,
            fontSize = 15.sp,
            color = textColor,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(tagColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${record.emotion_emoji} ${record.emotion_name}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
                )
            }

            Text(
                text = record.created_at,
                fontSize = 11.sp,
                color = subTextColor
            )
        }
    }
}

// ── Preview ───────────────────────────────────
@Preview(showBackground = true, name = "EmotionDetail Light")
@Composable
fun EmotionDetailLightPreview() {
    EmotionDetailScreen(
        emotion = sampleEmotions[0]
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF141414, name = "EmotionDetail Dark")
@Composable
fun EmotionDetailDarkPreview() {
    EmotionDetailScreen(
        emotion = sampleEmotions[0],
        isDark = true
    )
}