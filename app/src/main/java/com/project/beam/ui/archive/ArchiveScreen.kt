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
import com.project.beam.ui.home.DarkModeToggle
import com.project.beam.ui.home.LottieIcon
import com.project.beam.ui.home.emotionCardStyleMap
import com.project.beam.ui.theme.*
import com.project.beam.viewmodel.EmotionViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

val emotionColors = mapOf(
    "짜증" to Color(0xFFFF5252),
    "우울" to Color(0xFF3D5AFE),
    "슬픔" to Color(0xFF40C4FF),
    "외로움" to Color(0xFF7C4DFF),
    "행복" to Color(0xFFFFD700)
)

data class GraphPoint(
    val date: String,
    val counts: Map<String, Int>
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
                    DarkModeToggle(
                        isDark = isDark,
                        onToggle = { onDarkModeToggle(it) }
                    )
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
                    subTextColor = subTextColor,
                    records = homeState.records
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── 기록 리스트 ──
            if (homeState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
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
    subTextColor: Color,
    records: List<RecordResponse>
) {
    var selectedDateIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    val graphPoints = remember(records) {
        val grouped = records.groupBy { it.created_at.take(10) }
        val dailyMap = grouped.mapValues { (_, dayRecords) ->
            dayRecords.groupBy { it.category }.mapValues { it.value.size }
        }
        val today = LocalDate.now()

        // 오늘 기준 최근 5일 무조건 포함
        val defaultDates = (4 downTo 0).map { today.minusDays(it.toLong()) }

        // 데이터 있는 날짜 중 기본 5일에 없는 것도 추가
        val extraDates = dailyMap.keys
            .map { LocalDate.parse(it) }
            .filter { it !in defaultDates }
            .sortedDescending()

        val allDates = (defaultDates + extraDates).sortedBy { it }

        allDates.map { date ->
            val counts = dailyMap[date.toString()] ?: emptyMap()
            GraphPoint(
                date = "${date.monthValue}/${date.dayOfMonth}",
                counts = counts
            )
        }
    }

    LaunchedEffect(graphPoints) {
        animationProgress.snapTo(0f)
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
            if (graphPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "아직 데이터가 없어요", color = subTextColor, fontSize = 13.sp)
                }
            } else {
                val chartHeight = 200.dp
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
                                    val leftPadding = w * 0.10f
                                    val rightPadding = w * 0.05f
                                    val chartW = w - leftPadding - rightPadding

                                    if (graphPoints.size == 1) {
                                        selectedDateIndex = if (selectedDateIndex == 0) null else 0
                                        return@detectTapGestures
                                    }

                                    val segmentWidth = chartW / (graphPoints.size - 1)
                                    val index = ((offset.x - leftPadding) / segmentWidth)
                                        .toInt().coerceIn(0, graphPoints.size - 1)
                                    selectedDateIndex = if (selectedDateIndex == index) null else index
                                }
                            }
                            .onGloballyPositioned { chartWidthPx.value = it.size.width.toFloat() }
                    ) {
                        val w = size.width
                        val h = size.height
                        val leftPadding = w * 0.10f
                        val rightPadding = w * 0.05f
                        val chartW = w - leftPadding - rightPadding
                        val paddingBottom = 36f
                        val paddingTop = 16f
                        val chartH = h - paddingBottom - paddingTop
                        val maxVal = 5f
                        val yStepCount = 5

                        val segmentWidth = if (graphPoints.size > 1)
                            chartW / (graphPoints.size - 1) else chartW / 2f

                        val yLabelPaint = android.graphics.Paint().apply {
                            this.color = subTextColor.toArgb()
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        val xLabelPaint = android.graphics.Paint().apply {
                            this.color = subTextColor.toArgb()
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        // Y축 가이드라인 + 숫자
                        for (i in 0..yStepCount) {
                            val y = paddingTop + chartH - (i.toFloat() / yStepCount * chartH)
                            drawLine(
                                color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                                start = androidx.compose.ui.geometry.Offset(leftPadding, y),
                                end = androidx.compose.ui.geometry.Offset(w - rightPadding, y),
                                strokeWidth = 1f
                            )
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawText(
                                    i.toString(),
                                    leftPadding - 8f,
                                    y + 8f,
                                    yLabelPaint
                                )
                            }
                        }

                        // Y축 선
                        drawLine(
                            color = if (isDark) Color(0xFF3A3A3A) else Color(0xFFDDDDDD),
                            start = androidx.compose.ui.geometry.Offset(leftPadding, paddingTop),
                            end = androidx.compose.ui.geometry.Offset(leftPadding, paddingTop + chartH),
                            strokeWidth = 1.5f
                        )

                        // 선택된 날짜 수직선
                        selectedDateIndex?.let { idx ->
                            val x = if (graphPoints.size == 1) leftPadding + chartW / 2f
                            else leftPadding + idx * segmentWidth
                            drawLine(
                                color = if (isDark) Color(0xFF555555) else Color(0xFFCCCCCC),
                                start = androidx.compose.ui.geometry.Offset(x, paddingTop),
                                end = androidx.compose.ui.geometry.Offset(x, paddingTop + chartH),
                                strokeWidth = 1.5f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                        }

                        // 각 감정 라인
                        emotions.forEach { emotion ->
                            val color = emotionColors[emotion] ?: Color.Gray
                            val points = graphPoints.mapIndexed { index, data ->
                                val x = if (graphPoints.size == 1) leftPadding + chartW / 2f
                                else leftPadding + index * segmentWidth
                                val value = (data.counts[emotion] ?: 0).toFloat()
                                val y = paddingTop + chartH - (value / maxVal * chartH)
                                androidx.compose.ui.geometry.Offset(x, y)
                            }

                            if (points.size == 1) {
                                val value = graphPoints[0].counts[emotion] ?: 0
                                if (value > 0) {
                                    drawCircle(color = color, radius = 6f, center = points[0])
                                }
                                selectedDateIndex?.let {
                                    if (value > 0) {
                                        drawCircle(color = color, radius = 9f, center = points[0])
                                        drawCircle(color = Color.White, radius = 4f, center = points[0])
                                    }
                                }
                                return@forEach
                            }

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
                                    strokeWidth = 3f,
                                    cap = StrokeCap.Round
                                )
                            }
                            points.take(fullSegments + 1).forEachIndexed { idx, point ->
                                val value = graphPoints.getOrNull(idx)?.counts?.get(emotion) ?: 0
                                if (value > 0) {
                                    drawCircle(color = color, radius = 4f, center = point)
                                }
                            }
                            selectedDateIndex?.let { idx ->
                                if (idx < points.size) {
                                    val value = graphPoints[idx].counts[emotion] ?: 0
                                    if (value > 0) {
                                        drawCircle(color = color, radius = 8f, center = points[idx])
                                        drawCircle(color = Color.White, radius = 3.5f, center = points[idx])
                                    }
                                }
                            }
                        }

                        // X축 날짜
                        val showEvery = when {
                            graphPoints.size <= 7 -> 1
                            graphPoints.size <= 14 -> 2
                            else -> 3
                        }
                        drawIntoCanvas { canvas ->
                            graphPoints.forEachIndexed { index, data ->
                                if (index % showEvery == 0 || index == graphPoints.size - 1) {
                                    val x = if (graphPoints.size == 1) leftPadding + chartW / 2f
                                    else leftPadding + index * segmentWidth
                                    canvas.nativeCanvas.drawText(data.date, x, h - 4f, xLabelPaint)
                                }
                            }
                        }
                    }

                    // 툴팁
                    selectedDateIndex?.let { idx ->
                        val data = graphPoints.getOrNull(idx) ?: return@let
                        val w = chartWidthPx.value
                        if (w > 0f) {
                            val leftPadding = w * 0.10f
                            val rightPadding = w * 0.05f
                            val chartW = w - leftPadding - rightPadding
                            val segmentWidth = if (graphPoints.size > 1)
                                chartW / (graphPoints.size - 1) else chartW / 2f
                            val x = if (graphPoints.size == 1) leftPadding + chartW / 2f
                            else leftPadding + idx * segmentWidth
                            val xRatio = x / w

                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .align(if (xRatio > 0.5f) Alignment.TopStart else Alignment.TopEnd)
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5))
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0),
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
                                        Spacer(modifier = Modifier.height(4.dp))
                                        emotions.forEach { emotion ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            ) {
                                                val style = emotionCardStyleMap[emotion]
                                                val lottieRes = style?.lottieRes
                                                if (lottieRes != null && lottieRes != 0) {
                                                    LottieIcon(
                                                        resId = lottieRes,
                                                        size = 16.dp,
                                                        isPlaying = true
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = "$emotion : ${data.counts[emotion] ?: 0}",
                                                    fontSize = 11.sp,
                                                    color = emotionColors[emotion] ?: subTextColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
            Text(text = formattedDate, fontSize = 11.sp, color = subTextColor)
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