package com.project.beam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.data.emotion.RecordResponse
import com.project.beam.ui.theme.*
import com.project.beam.viewmodel.EmotionCardUi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.delay

@Composable
fun EmotionDetailScreen(
    emotionCardUi: EmotionCardUi,
    isDark: Boolean,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface
    val style = emotionCardStyleMap[emotionCardUi.name]
    val tagColor = if (isDark) style?.darkColor ?: DarkSurface
    else style?.lightColor ?: LightSurface

    Scaffold(containerColor = bgColor) { innerPadding ->
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
                DarkModeToggle(
                    isDark = isDark,
                    onToggle = { onDarkModeToggle(it) }
                )
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
                    Text(text = "감정 기록", fontSize = 12.sp, color = subTextColor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Lottie 아이콘
                        val lottieRes = style?.lottieRes
                        if (lottieRes != null && lottieRes != 0) {
                            LottieIcon(
                                resId = lottieRes,
                                size = 32.dp,
                                isPlaying = true
                            )
                        } else {
                            Text(text = emotionCardUi.emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = emotionCardUi.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            // ── 기록 리스트 ──
            if (emotionCardUi.records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 기록이 없어요",
                        color = subTextColor,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(emotionCardUi.records) { index, record ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(100L + index * 80L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(350)) + slideInVertically(
                                animationSpec = tween(350),
                                initialOffsetY = { it / 2 }
                            )
                        ) {
                            EmotionRecordItem(
                                record = record,
                                isDark = isDark,
                                cardBg = cardBg,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                tagColor = tagColor,
                                style = style
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

// ── 기록 아이템 카드 ──────────────────────────
@Composable
fun EmotionRecordItem(
    record: RecordResponse,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    tagColor: Color,
    style: com.project.beam.data.emotion.EmotionCard?
) {
    val formattedDate = remember(record.created_at) {
        try {
            record.created_at.substring(0, 10).replace("-", ".")
        } catch (e: Exception) {
            record.created_at
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                RoundedCornerShape(16.dp)
            )
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
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(tagColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lottieRes = style?.lottieRes
                if (lottieRes != null && lottieRes != 0) {
                    LottieIcon(resId = lottieRes, size = 18.dp, isPlaying = true)
                } else {
                    Text(text = style?.emoji ?: "", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = record.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
                )
            }
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = subTextColor
            )
        }
    }
}