package com.project.beam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.project.beam.R
import com.project.beam.data.emotion.EmotionCard
import com.project.beam.data.emotion.RecordResponse
import com.project.beam.ui.theme.*
import com.project.beam.viewmodel.EmotionCardUi
import com.project.beam.viewmodel.EmotionViewModel
import com.project.beam.viewmodel.RecordSubmitState

// ── 감정 컬러 매핑 ────────────────────────────
val emotionCardStyleMap = mapOf(
    "행복" to EmotionCard("☀️", "행복", 0, Color(0xFFFFF3C4), Color(0xFF4A3A00), Color(0xFFFFE066), Color(0xFFFFD700)),
    "우울" to EmotionCard("🌧", "우울", 0, Color(0xFFDAE0F5), Color(0xFF1A2456), Color(0xFF8FA8FF), Color(0xFF3D5AFE)),
    "외로움" to EmotionCard("🌙", "외로움", 0, Color(0xFFE8DCFF), Color(0xFF2D1F5E), Color(0xFFB39DFF), Color(0xFF7C4DFF)),
    "짜증" to EmotionCard("😤", "짜증", 0, Color(0xFFFFD6D6), Color(0xFF5C1A1A), Color(0xFFFF8A80), Color(0xFFFF5252)),
    "슬픔" to EmotionCard("💧", "슬픔", 0, Color(0xFFD6F0FF), Color(0xFF0A3040), Color(0xFF80D8FF), Color(0xFF40C4FF))
)

// ── HomeScreen ────────────────────────────────
@Composable
fun HomeScreen(
    isDark: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    showBottomSheet: Boolean,
    onBottomSheetDismiss: () -> Unit,
    onSloganClick: () -> Unit = {},
    onEmotionClick: (EmotionCardUi) -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface
    var selectedSlogan by remember { mutableStateOf<String?>(null) }

    val viewModel = remember { EmotionViewModel() }
    val homeState by viewModel.homeState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    val context = LocalContext.current
    val slogan by viewModel.slogan.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
        viewModel.loadSlogan(context)
    }

    LaunchedEffect(submitState) {
        if (submitState is RecordSubmitState.Success) {
            onBottomSheetDismiss()
            viewModel.resetSubmitState()
        }
    }

    if (showBottomSheet) {
        AddEmotionBottomSheet(
            isDark = isDark,
            onDismiss = {
                onBottomSheetDismiss()
                selectedSlogan = null
            },
            onSubmit = { content -> viewModel.createRecord(content) },
            isLoading = submitState is RecordSubmitState.Loading,
            hint = selectedSlogan
        )
    }

    Scaffold(containerColor = bgColor) { innerPadding ->
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
                            Text(text = if (isDark) "🌙" else "☀️", fontSize = 10.sp)
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
                    text = java.text.SimpleDateFormat("M월 d일 (E)", java.util.Locale.KOREAN)
                        .format(java.util.Date()),
                    fontSize = 13.sp,
                    color = subTextColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 오늘의 질문 카드 ──
            slogan?.let { question ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark)
                                    listOf(Color(0xFF2A2A2A), Color(0xFF1E1E1E))
                                else
                                    listOf(Color(0xFFF5F0FF), Color(0xFFEDE8FF))
                            )
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF3A3A3A) else Color(0xFFD4C8FF),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedSlogan = question
                            onSloganClick()
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "오늘의 질문 ✨",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "여기에 남기기 →",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 로딩 / 감정 카드 ──
            if (homeState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = textColor)
                }
            } else if (homeState.emotionCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 기록이 없어요\n첫 감정을 남겨보세요 💭",
                        color = subTextColor,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            } else {
                EmotionGrid(
                    emotionCards = homeState.emotionCards,
                    isDark = isDark,
                    onEmotionClick = onEmotionClick
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 최근 기록 ──
            if (homeState.records.isNotEmpty()) {
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
                    items(homeState.records.take(10)) { record ->
                        val style = emotionCardStyleMap[record.category]
                        RecentRecordCard(
                            record = record,
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor,
                            emotionStyle = style
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ── 감정 카드 그리드 ──────────────────────────
@Composable
fun EmotionGrid(
    emotionCards: List<EmotionCardUi>,
    isDark: Boolean,
    onEmotionClick: (EmotionCardUi) -> Unit = {}
) {
    val firstRow = emotionCards.take(2)
    val secondRow = emotionCards.drop(2).take(3)

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            firstRow.forEach { card ->
                val style = emotionCardStyleMap[card.name]
                EmotionCardItem(
                    name = card.name,
                    emoji = card.emoji,
                    count = card.count,
                    style = style,
                    isDark = isDark,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { onEmotionClick(card) }
                )
            }
            // 카드가 1개면 빈 공간 채우기
            if (firstRow.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        if (secondRow.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                secondRow.forEach { card ->
                    val style = emotionCardStyleMap[card.name]
                    EmotionCardItem(
                        name = card.name,
                        emoji = card.emoji,
                        count = card.count,
                        style = style,
                        isDark = isDark,
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clickable { onEmotionClick(card) }
                    )
                }
                repeat(3 - secondRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── 감정 카드 아이템 ──────────────────────────
@Composable
fun EmotionCardItem(
    name: String,
    emoji: String,
    count: Int,
    style: EmotionCard?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (isDark) style?.darkColor ?: Color(0xFF2A2A2A)
    else style?.lightColor ?: Color(0xFFEEEEEE)
    val glowColor = if (isDark) style?.darkGlow ?: Color(0xFF444444)
    else style?.lightGlow ?: Color(0xFFDDDDDD)

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text = "${count}개",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
            )
            Text(
                text = "유물 ${count}개",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
            )
        }
    }
}

// ── 최근 기록 카드 ────────────────────────────
@Composable
fun RecentRecordCard(
    record: RecordResponse,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    emotionStyle: EmotionCard?
) {
    val tagColor = if (isDark) emotionStyle?.darkColor ?: DarkSurface
    else emotionStyle?.lightColor ?: LightSurface

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
                text = "${emotionStyle?.emoji ?: ""} ${record.category}",
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
                Text(text = "유물 등록", fontSize = 10.sp, color = subTextColor)
            }

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
                Text(text = "연대기", fontSize = 10.sp, color = subTextColor)
            }
        }
    }
}

// ── Lottie 아이콘 ─────────────────────────────
@Composable
fun LottieIcon(
    resId: Int,
    size: Dp,
    isPlaying: Boolean = false
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
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