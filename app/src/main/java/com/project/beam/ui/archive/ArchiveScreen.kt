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
import com.project.beam.data.emotion.RecordResponse
import com.project.beam.ui.home.emotionCardStyleMap
import com.project.beam.ui.theme.*
import com.project.beam.viewmodel.EmotionViewModel
import kotlinx.coroutines.delay

val emotionColors = mapOf(
    "짜증" to Color(0xFFFF5252),
    "우울" to Color(0xFF3D5AFE),
    "슬픔" to Color(0xFF40C4FF),
    "외로움" to Color(0xFF7C4DFF),
    "행복" to Color(0xFFFFD700)
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

@Composable
fun ArchiveScreen(
    isDark: Boolean,
    onDarkModeToggle: (Boolean) -> Unit = {}
) {
    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val cardBg = if (isDark) DarkSurface else LightSurface

    val viewModel = remember { EmotionViewModel() }
    val homeState by viewModel.homeState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Scaffold(containerColor = bgColor) { innerPadding ->
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

            // ── 기록 리스트 ──
            if (homeState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = textColor) }
                }
            } else {
                items(homeState.records.size) { index ->
                    val record = homeState.records[index]
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
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor,
                            subTextColor = subTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

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
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val emotions = listOf("짜증", "우울", "슬픔", "외로움", "행복")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            val chartHeight = 180.dp
            val chartWidthPx = remember { mutableStateOf(0f) }
            val progress = animationProgress.value

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val w = chartWidthPx.value
                                if (w == 0f) return@detectTapGestures
                                val leftPadding = w * 0.05f
                                val chartW = w - leftPadding * 2
                                val segmentWidth = chartW / (graphData.size - 1)
                                val index = ((offset.x - leftPadding) / segmentWidth)
                                    .toInt().coerceIn(0, graphData.size - 1)
                                selectedDateIndex = if (selectedDateIndex == index) null else index
                            }
                        }
                        .onGloballyPositioned { chartWidthPx.value = it.size.width.toFloat() }
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = 4f
                    val leftPadding = w * 0.05f
                    val chartW = w - leftPadding * 2
                    val segmentWidth = chartW / (graphData.size - 1)
                    val paddingBottom = 30f
                    val paddingTop = 10f
                    val chartH = h - paddingBottom - paddingTop

                    for (i in 0..4) {
                        val y = paddingTop + chartH - (i / maxVal * chartH)
                        drawLine(
                            color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                            start = androidx.compose.ui.geometry.Offset(leftPadding, y),
                            end = androidx.compose.ui.geometry.Offset(w - leftPadding, y),
                            strokeWidth = 1f
                        )
                    }

                    selectedDateIndex?.let { idx ->
                        val x = leftPadding + idx * segmentWidth
                        drawLine(
                            color = if (isDark) Color(0xFF555555) else Color(0xFFCCCCCC),
                            start = androidx.compose.ui.geometry.Offset(x, paddingTop),
                            end = androidx.compose.ui.geometry.Offset(x, paddingTop + chartH),
                            strokeWidth = 1.5f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    }

                    emotions.forEach { emotion ->
                        val color = emotionColors[emotion] ?: Color.Gray
                        val points = graphData.mapIndexed { index, data ->
                            val x = leftPadding + index * segmentWidth
                            val value = (data.counts[emotion] ?: 0).toFloat()
                            val y = paddingTop + chartH - (value / maxVal * chartH)
                            androidx.compose.ui.geometry.Offset(x, y)
                        }
                        val totalPoints = points.size
                        val animatedFloat = progress * (totalPoints - 1)
                        val fullSegments = animatedFloat.toInt()
                        val partialProgress = animatedFloat - fullSegments

                        for (i in 0 until fullSegments.coerceAtMost(totalPoints - 1)) {
                            drawLine(color = color, start = points[i], end = points[i + 1], strokeWidth = 3f, cap = StrokeCap.Round)
                        }
                        if (fullSegments < totalPoints - 1) {
                            val start = points[fullSegments]
                            val end = points[fullSegments + 1]
                            drawLine(
                                color = color,
                                start = start,
                                end = androidx.compose.ui.geometry.Offset(
                                    start.x + (end.x - start.x) * partialProgress,
                                    start.y + (end.y - start.y) * partialProgress
                                ),
                                strokeWidth = 3f, cap = StrokeCap.Round
                            )
                        }
                        points.take(fullSegments + 1).forEach { point ->
                            drawCircle(color = color, radius = 4f, center = point)
                        }
                        selectedDateIndex?.let { idx ->
                            if (idx < points.size) {
                                drawCircle(color = color, radius = 8f, center = points[idx])
                                drawCircle(color = Color.White, radius = 3.5f, center = points[idx])
                            }
                        }
                    }

                    val paint = android.graphics.Paint().apply {
                        this.color = subTextColor.toArgb()
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawIntoCanvas { canvas ->
                        graphData.forEachIndexed { index, data ->
                            canvas.nativeCanvas.drawText(data.date, leftPadding + index * segmentWidth, h, paint)
                        }
                    }
                }

                selectedDateIndex?.let { idx ->
                    val data = graphData[idx]
                    val w = chartWidthPx.value
                    if (w > 0f) {
                        val leftPadding = w * 0.05f
                        val chartW = w - leftPadding * 2
                        val segmentWidth = chartW / (graphData.size - 1)
                        val xRatio = (leftPadding + idx * segmentWidth) / w
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(if (xRatio > 0.5f) Alignment.TopStart else Alignment.TopEnd)
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5))
                                    .border(1.dp, if (isDark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(text = data.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    emotions.forEach { emotion ->
                                        Text(
                                            text = "$emotion : ${data.counts[emotion] ?: 0}",
                                            fontSize = 11.sp,
                                            color = emotionColors[emotion] ?: subTextColor,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

@Composable
fun ArchiveRecordItem(
    record: RecordResponse,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color
) {
    val style = emotionCardStyleMap[record.category]
    val tagColor = if (isDark) style?.darkColor ?: DarkSurface
    else style?.lightColor ?: LightSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = record.content, fontSize = 14.sp, color = textColor, lineHeight = 20.sp)
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
                    text = "${style?.emoji ?: ""} ${record.category}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
                )
            }
            Text(text = record.created_at, fontSize = 11.sp, color = subTextColor)
        }
    }
}

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