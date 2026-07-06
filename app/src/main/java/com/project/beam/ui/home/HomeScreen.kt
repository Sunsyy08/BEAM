package com.project.beam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.data.emotion.EmotionCard
import com.project.beam.data.emotion.RecentRecordResponse
import com.project.beam.ui.theme.*
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.*
import com.project.beam.R
import androidx.compose.foundation.clickable

// ── 샘플 데이터 ──────────────────────────────
val sampleEmotions = listOf(
    EmotionCard("☀️", "행복", 4, Color(0xFFFFF3C4), Color(0xFF4A3A00), Color(0xFFFFE066), Color(0xFFFFD700)),
    EmotionCard("🌧", "우울", 3, Color(0xFFDAE0F5), Color(0xFF1A2456), Color(0xFF8FA8FF), Color(0xFF3D5AFE)),
    EmotionCard("🌙", "외로움", 3, Color(0xFFE8DCFF), Color(0xFF2D1F5E), Color(0xFFB39DFF), Color(0xFF7C4DFF)),
    EmotionCard("😤", "짜증", 2, Color(0xFFFFD6D6), Color(0xFF5C1A1A), Color(0xFFFF8A80), Color(0xFFFF5252)),
    EmotionCard("💧", "슬픔", 2, Color(0xFFD6F0FF), Color(0xFF0A3040), Color(0xFF80D8FF), Color(0xFF40C4FF))
)

val sampleRecords = listOf(
    RecentRecordResponse(1, "미안하다는 말 대신 화를 냈어. 그때 참았어야 했는데.", "짜증", "😤", "2025-07-06"),
    RecentRecordResponse(2, "요즘 아무것도 하기 싫고 그냥 누워만 있고 싶다.", "우울", "🌧", "2025-07-06"),
    RecentRecordResponse(3, "오늘 괜찮은 하루였다. 작은 것에 감사.", "행복", "☀️", "2025-07-06")
)

// ── HomeScreen ────────────────────────────────
@Composable
fun HomeScreen(
    isDark: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    showBottomSheet: Boolean,
    onBottomSheetDismiss: () -> Unit,
    onEmotionClick: (Int) -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface

    if (showBottomSheet) {
        AddEmotionBottomSheet(
            isDark = isDark,
            onDismiss = onBottomSheetDismiss,
            onSubmit = { }
        )
    }

    Scaffold(
        containerColor = bgColor,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
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
                // 다크모드 스위치
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0))
                        .border(1.dp, if (isDark) Color(0xFF555555) else Color(0xFFCCCCCC), CircleShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = if (isDark) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDark) "🌙" else "☀️",
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onDarkModeToggle(it) },
                        modifier = Modifier.fillMaxSize(),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Transparent,
                            uncheckedThumbColor = Color.Transparent,
                            checkedTrackColor = Color.Transparent,
                            uncheckedTrackColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── 타이틀 ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "오늘도 여기 두고 가요",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "7월 6일 (월)",
                    fontSize = 13.sp,
                    color = subTextColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 감정 카드 그리드 ──
            EmotionGrid(
                emotions = sampleEmotions,
                isDark = isDark,
                onEmotionClick = onEmotionClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 최근 기록 ──
            Text(
                text = "최근 기록",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleRecords.size) { index ->
                    val record = sampleRecords[index]
                    val emotion = sampleEmotions.find { it.name == record.emotion_name }
                    RecentRecordCard(
                        record = record,
                        isDark = isDark,
                        cardBg = cardBg,
                        textColor = textColor,
                        emotion = emotion
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ── 감정 카드 그리드 ──────────────────────────
@Composable
fun EmotionGrid(
    emotions: List<EmotionCard>,
    isDark: Boolean,
    onEmotionClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            emotions.take(2).forEachIndexed { index, emotion ->
                EmotionCardItem(
                    emotion = emotion,
                    isDark = isDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { onEmotionClick(index) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            emotions.drop(2).forEachIndexed { index, emotion ->
                EmotionCardItem(
                    emotion = emotion,
                    isDark = isDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { onEmotionClick(index + 2) }
                )
            }
        }
    }
}

// ── 감정 카드 아이템 (글래스모피즘) ───────────
@Composable
fun EmotionCardItem(
    emotion: EmotionCard,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (isDark) emotion.darkColor else emotion.lightColor
    val glowColor = if (isDark) emotion.darkGlow else emotion.lightGlow

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isDark) 0.3f else 0.5f),
                        baseColor
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.15f else 0.6f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        // 글로우 오버레이
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = if (isDark) 0.4f else 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 상단: 이모지 + 개수
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emotion.emoji, fontSize = 22.sp)
            Text(
                text = "${emotion.count}개",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }

        // 하단: 감정명 + 유물 개수
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = emotion.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
            )
            Text(
                text = "유물 ${emotion.count}개",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
            )
        }
    }
}

// ── 최근 기록 카드 ────────────────────────────
@Composable
fun RecentRecordCard(
    record: RecentRecordResponse,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    emotion: EmotionCard?
) {
    val tagColor = if (isDark) emotion?.darkColor ?: DarkSurface
    else emotion?.lightColor ?: LightSurface

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = record.content,
            fontSize = 13.sp,
            color = textColor,
            lineHeight = 18.sp,
            maxLines = 3,
            modifier = Modifier.height(60.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
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
    }
}

// ── 하단 네비게이션 바 ────────────────────────
@Composable
fun HomeBottomBar(
    isDark: Boolean,
    bgColor: Color,
    textColor: Color,
    subTextColor: Color,
    onAddClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    var homeClicked by remember { mutableStateOf(false) }
    var addClicked by remember { mutableStateOf(false) }
    var timelineClicked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(bottom = 20.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 홈
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    homeClicked = !homeClicked
                    onHomeClick()
                }
            ) {
                LottieIcon(
                    resId = if (isDark) R.raw.icon_home_dark else R.raw.icon_home_light,
                    size = 28.dp,
                    isPlaying = homeClicked
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "홈",
                    fontSize = 10.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 유물 등록 (+)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-8).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DarkText else LightText)
                        .clickable {
                            addClicked = !addClicked
                            onAddClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LottieIcon(
                        resId = if (isDark) R.raw.icon_add_light else R.raw.icon_add_dark,
                        size = 30.dp,
                        isPlaying = addClicked
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "유물 등록",
                    fontSize = 10.sp,
                    color = subTextColor
                )
            }

            // 연대기
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    timelineClicked = !timelineClicked
                    onArchiveClick()
                }
            ) {
                LottieIcon(
                    resId = if (isDark) R.raw.icon_timeline_dark else R.raw.icon_timeline_light,
                    size = 28.dp,
                    isPlaying = timelineClicked
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "연대기",
                    fontSize = 10.sp,
                    color = subTextColor
                )
            }
        }
    }
}

// ── Lottie 아이콘 컴포넌트 ────────────────────
@Composable
fun LottieIcon(
    resId: Int,
    size: Dp,
    isPlaying: Boolean = false
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(resId)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = 1,
        restartOnPlay = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(size)
    )
}