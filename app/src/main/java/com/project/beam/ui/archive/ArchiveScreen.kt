package com.project.beam.ui.archive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.data.emotion.EmotionCard
import com.project.beam.data.emotion.RecentRecordResponse
import com.project.beam.ui.home.HomeBottomBar
import com.project.beam.ui.home.sampleEmotions
import com.project.beam.ui.theme.*
import kotlinx.coroutines.delay

// ── 샘플 데이터 ──────────────────────────────
val archiveSampleRecords = listOf(
    RecentRecordResponse(1, "미안하다는 말 대신 화를 냈어. 그때 참았어야 했는데.", "짜증", "😤", "2026.07.05"),
    RecentRecordResponse(2, "요즘 아무것도 하기 싫고 그냥 누워만 있고 싶다.", "우울", "🌧", "2026.07.04"),
    RecentRecordResponse(3, "정말 고마웠어. 네가 없었다면 버티지 못했을 거야.", "행복", "☀️", "2026.07.04"),
    RecentRecordResponse(4, "왜 나한테만 그런 말을 했을까. 계속 생각나.", "짜증", "😤", "2026.07.03"),
    RecentRecordResponse(5, "혼자 있는 게 편한 날이 많아졌다.", "외로움", "🌙", "2026.07.02"),
    RecentRecordResponse(6, "오늘은 진짜 행복했다. 이유는 모르겠지만.", "행복", "☀️", "2026.07.01"),
)

data class DailyEmotionData(
    val date: String,
    val counts: Map<String, Int>
)

val graphData = listOf(
    DailyEmotionData("6/7",  mapOf("짜증" to 1, "우울" to 0, "슬픔" to 0, "외로움" to 0, "행복" to 1)),
    DailyEmotionData("6/14", mapOf("짜증" to 2, "우울" to 1, "슬픔" to 1, "외로움" to 0, "행복" to 2)),
    DailyEmotionData("6/21", mapOf("짜증" to 1, "우울" to 2, "슬픔" to 0, "외로움" to 1, "행복" to 3)),
    DailyEmotionData("6/28", mapOf("짜증" to 3, "우울" to 1, "슬픔" to 2, "외로움" to 2, "행복" to 1)),
    DailyEmotionData("7/6",  mapOf("짜증" to 2, "우울" to 0, "슬픔" to 1, "외로움" to 1, "행복" to 2)),
)

val emotionColors = mapOf(
    "짜증" to Color(0xFFFF5252),
    "우울" to Color(0xFF3D5AFE),
    "슬픔" to Color(0xFF40C4FF),
    "외로움" to Color(0xFF7C4DFF),
    "행복" to Color(0xFFFFD700)
)

// ── ArchiveScreen ─────────────────────────────
@Composable
fun ArchiveScreen(
    isDark: Boolean,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface

    Scaffold(
        containerColor = bgColor,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── 상단 바 ──
            item {
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
            }

            // ── 타이틀 ──
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(text = "모든 기록", fontSize = 12.sp, color = subTextColor)
                    Text(
                        text = "연대기",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 그래프 ──
            item {
                EmotionLineChart(
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── 기록 리스트 (애니메이션) ──
            items(archiveSampleRecords.size) { index ->
                val record = archiveSampleRecords[index]
                val emotion = sampleEmotions.find { it.name == record.emotion_name }

                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(200L + index * 120L)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        animationSpec = tween(400, easing = EaseOutCubic),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ArchiveRecordItem(
                        record = record,
                        emotion = emotion,
                        isDark = isDark,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ── 감정 라인 차트 ────────────────────────────
@Composable
fun EmotionLineChart(
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color
) {
    var selectedDateIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    val emotions = listOf("짜증", "우울", "슬픔", "외로움", "행복")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // 툴팁
            selectedDateIndex?.let { dateIndex ->
                val data = graphData[dateIndex]
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5))
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF3A3A3A) else Color(0xFFEEEEEE),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = data.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        emotions.forEach { emotion ->
                            Text(
                                text = "$emotion : ${data.counts[emotion] ?: 0}",
                                fontSize = 11.sp,
                                color = emotionColors[emotion] ?: subTextColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 캔버스 차트
            val chartHeight = 180.dp
            val chartWidthPx = remember { mutableStateOf(0f) }
            val progress = animationProgress.value

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = chartWidthPx.value
                            val segmentWidth = w / (graphData.size - 1)
                            val index = (offset.x / segmentWidth)
                                .toInt()
                                .coerceIn(0, graphData.size - 1)
                            selectedDateIndex =
                                if (selectedDateIndex == index) null else index
                        }
                    }
                    .onGloballyPositioned { chartWidthPx.value = it.size.width.toFloat() }
            ) {
                val w = size.width
                val h = size.height
                val maxVal = 4f
                val segmentWidth = w / (graphData.size - 1)
                val paddingBottom = 30f
                val paddingTop = 10f
                val chartH = h - paddingBottom - paddingTop

                // Y축 가이드라인
                for (i in 0..4) {
                    val y = paddingTop + chartH - (i / maxVal * chartH)
                    drawLine(
                        color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // 각 감정 라인
                emotions.forEach { emotion ->
                    val color = emotionColors[emotion] ?: Color.Gray
                    val points = graphData.mapIndexed { index, data ->
                        val x = index * segmentWidth
                        val value = (data.counts[emotion] ?: 0).toFloat()
                        val y = paddingTop + chartH - (value / maxVal * chartH)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    // 전체 경로 길이 기반 애니메이션
                    val totalPoints = points.size
                    val animatedFloat = progress * (totalPoints - 1)
                    val fullSegments = animatedFloat.toInt()
                    val partialProgress = animatedFloat - fullSegments

                    for (i in 0 until fullSegments.coerceAtMost(totalPoints - 1)) {
                        drawLine(
                            color = color,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // 마지막 진행 중인 세그먼트
                    if (fullSegments < totalPoints - 1) {
                        val start = points[fullSegments]
                        val end = points[fullSegments + 1]
                        val partialEnd = androidx.compose.ui.geometry.Offset(
                            start.x + (end.x - start.x) * partialProgress,
                            start.y + (end.y - start.y) * partialProgress
                        )
                        drawLine(
                            color = color,
                            start = start,
                            end = partialEnd,
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // 꼭짓점 점
                    points.take(fullSegments + 1).forEach { point ->
                        drawCircle(color = color, radius = 5f, center = point)
                    }

                    // 선택된 날짜 꼭짓점 강조
                    selectedDateIndex?.let { idx ->
                        if (idx < points.size) {
                            drawCircle(color = color, radius = 9f, center = points[idx])
                            drawCircle(color = Color.White, radius = 4f, center = points[idx])
                        }
                    }
                }

                // X축 날짜 텍스트
                val paint = android.graphics.Paint().apply {
                    this.color = subTextColor.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawIntoCanvas { canvas ->
                    graphData.forEachIndexed { index, data ->
                        canvas.nativeCanvas.drawText(
                            data.date,
                            index * segmentWidth,
                            h,
                            paint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 범례
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                emotions.forEach { emotion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(emotionColors[emotion] ?: Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = emotion, fontSize = 10.sp, color = subTextColor)
                    }
                }
            }
        }
    }
}

// ── 기록 아이템 ───────────────────────────────
@Composable
fun ArchiveRecordItem(
    record: RecentRecordResponse,
    emotion: EmotionCard?,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color
) {
    val tagColor = if (isDark) emotion?.darkColor ?: DarkSurface
    else emotion?.lightColor ?: LightSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
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
            fontSize = 14.sp,
            color = textColor,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
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
            Text(text = record.created_at, fontSize = 11.sp, color = subTextColor)
        }
    }
}

// ── Preview ───────────────────────────────────
@Preview(showBackground = true, name = "Archive Light")
@Composable
fun ArchiveScreenLightPreview() {
    ArchiveScreen(isDark = false)
}

@Preview(showBackground = true, backgroundColor = 0xFF141414, name = "Archive Dark")
@Composable
fun ArchiveScreenDarkPreview() {
    ArchiveScreen(isDark = true)
}