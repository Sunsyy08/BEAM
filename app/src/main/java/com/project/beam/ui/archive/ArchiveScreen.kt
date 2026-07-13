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
import androidx.compose.ui.draw.alpha
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
import java.time.format.TextStyle
import java.util.Locale

val emotionColors = mapOf(
    "짜증" to Color(0xFFFF5252),
    "우울" to Color(0xFF3D5AFE),
    "슬픔" to Color(0xFF40C4FF),
    "외로움" to Color(0xFF7C4DFF),
    "행복" to Color(0xFFFFD700)
)

val emotionOrder = listOf("행복", "짜증", "우울", "슬픔", "외로움")

data class GraphPoint(
    val date: String,
    val counts: Map<String, Int>
)

// ── 감정 날씨 계산 ────────────────────────────
fun getWeatherInfo(records: List<RecordResponse>): Triple<String, String, String> {
    if (records.isEmpty()) return Triple("💭", "아직 기록이 없어요", "감정을 담아보세요")

    val today = LocalDate.now()
    val thisMonth = today.monthValue
    val monthRecords = records.filter {
        it.created_at.take(7) == "${today.year}-${thisMonth.toString().padStart(2, '0')}"
    }
    if (monthRecords.isEmpty()) return Triple("💭", "이번 달 기록이 없어요", "첫 감정을 담아보세요")

    val sorted = monthRecords.sortedBy { it.created_at }
    val total = sorted.size
    val firstHalf = sorted.take(total / 2)
    val secondHalf = sorted.drop(total / 2)

    fun dominant(list: List<RecordResponse>) =
        list.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "행복"

    val earlyEmotion = dominant(firstHalf)
    val lateEmotion = dominant(secondHalf)
    val topEmotion = monthRecords.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "행복"

    val (emoji, headline) = when {
        earlyEmotion == lateEmotion && lateEmotion == "행복" -> "☀️" to "맑음이에요"
        earlyEmotion != "행복" && lateEmotion == "행복" -> "⛅" to "흐리다 맑음이에요"
        earlyEmotion == "행복" && lateEmotion != "행복" -> "🌥" to "맑다 흐려졌어요"
        topEmotion == "우울" || topEmotion == "슬픔" -> "🌧" to "흐린 날이 많았어요"
        topEmotion == "짜증" -> "⛈" to "폭풍 같은 달이었어요"
        topEmotion == "외로움" -> "🌙" to "조용한 달이었어요"
        else -> "🌤" to "그럭저럭 괜찮았어요"
    }

    val sub = "총 ${monthRecords.size}번 감정을 담았어요"
    return Triple(emoji, headline, sub)
}

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

    val monthlyReport by viewModel.monthlyReport.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
        viewModel.loadMonthlyReport()
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
                    DarkModeToggle(isDark = isDark, onToggle = { onDarkModeToggle(it) })
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

            // ── 감정 날씨 리포트 ──
            item {
                val today = LocalDate.now()
                val monthLabel = "${today.monthValue}월"

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "감정 날씨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isDark) Color(0xFF2D1F5E) else Color(0xFFEDE8FF)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = monthLabel,
                                fontSize = 10.sp,
                                color = if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
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
                        if (monthlyReport == null) {
                            // 로딩 or 데이터 없을 때
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(text = "💭", fontSize = 40.sp)
                                Column {
                                    Text(
                                        text = "아직 기록이 없어요",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "감정을 담으면 날씨가 보여요",
                                        fontSize = 12.sp,
                                        color = subTextColor
                                    )
                                }
                            }
                        } else {
                            val today = LocalDate.now()
                            val prefix = "${today.year}-${today.monthValue.toString().padStart(2, '0')}"
                            val monthRecords = homeState.records
                                .filter { it.created_at.take(7) == prefix }
                                .sortedBy { it.created_at }

                            val totalCount = monthRecords.size

                            val third = (monthRecords.size / 3).coerceAtLeast(1)
                            val earlyRecords = monthRecords.take(third)
                            val midRecords = monthRecords.drop(third).take(third)
                            val lateRecords = monthRecords.drop(third * 2)

                            fun dominant(list: List<RecordResponse>) =
                                list.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: ""

                            val earlyEmotion = dominant(earlyRecords)
                            val midEmotion = dominant(midRecords)
                            val lateEmotion = dominant(lateRecords)

                            fun emotionEmoji(name: String) = when (name) {
                                "행복" -> "☀️"
                                "우울" -> "🌧"
                                "짜증" -> "😤"
                                "슬픔" -> "💧"
                                "외로움" -> "🌙"
                                else -> "💭"
                            }

                            val emoji = when {
                                monthlyReport!!.weather_comment.contains("어둡") -> "🌧"
                                monthlyReport!!.weather_comment.contains("흐린") -> "⛅"
                                monthlyReport!!.weather_comment.contains("폭풍") -> "⛈"
                                monthlyReport!!.weather_comment.contains("맑") -> "☀️"
                                monthlyReport!!.weather_comment.contains("행복") -> "🌤"
                                lateEmotion == "행복" -> "☀️"
                                lateEmotion == "우울" -> "🌧"
                                lateEmotion == "짜증" -> "⛈"
                                lateEmotion == "슬픔" -> "🌦"
                                lateEmotion == "외로움" -> "🌙"
                                else -> "⛅"
                            }

                            val titleText = when {
                                earlyEmotion.isNotEmpty() && lateEmotion.isNotEmpty() ->
                                    "\"$earlyEmotion → $lateEmotion\"이에요"
                                monthlyReport!!.weather_comment.contains("맑") -> "\"흐리다 맑음\"이에요"
                                monthlyReport!!.weather_comment.contains("행복") -> "\"점점 맑아지는 중\"이에요"
                                monthlyReport!!.weather_comment.contains("어둡") ||
                                        monthlyReport!!.weather_comment.contains("폭풍") -> "\"흐린 날이 많았어요\""
                                monthlyReport!!.weather_comment.contains("외로움") -> "\"조용한 달이었어요\""
                                else -> "\"이번 달 감정 날씨\""
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(text = emoji, fontSize = 44.sp)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = titleText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            lineHeight = 22.sp
                                        )
                                        Text(
                                            text = monthlyReport!!.weather_comment,
                                            fontSize = 12.sp,
                                            color = subTextColor,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                Divider(color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "초반" to earlyEmotion,
                                            "중반" to midEmotion,
                                            "후반" to lateEmotion
                                        ).forEachIndexed { index, (label, emotion) ->
                                            if (emotion.isNotEmpty()) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    val emotionStyle = emotionCardStyleMap[emotion]
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isDark) emotionStyle?.darkColor ?: Color(0xFF2A2A2A)
                                                                else emotionStyle?.lightColor ?: Color(0xFFEEEEEE)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = emotionEmoji(emotion), fontSize = 18.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(text = label, fontSize = 9.sp, color = subTextColor)
                                                }
                                                if (index < 2 && midEmotion.isNotEmpty()) {
                                                    Text(text = "→", fontSize = 14.sp, color = subTextColor)
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "총 ${totalCount}번",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "이번 달 기록",
                                            fontSize = 10.sp,
                                            color = subTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 감정 도감 ──
            item {
                val thisMonthRecords = remember(homeState.records) {
                    val today = LocalDate.now()
                    val prefix = "${today.year}-${today.monthValue.toString().padStart(2, '0')}"
                    homeState.records.filter { it.created_at.take(7) == prefix }
                }
                val collectedMap = remember(thisMonthRecords) {
                    thisMonthRecords.groupBy { it.category }.mapValues { it.value.size }
                }
                val collectedCount = collectedMap.keys.size

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "감정 도감",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Text(
                            text = "$collectedCount / ${emotionOrder.size} 수집",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
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
                        Column {
                            // 도감 아이콘 5개
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                emotionOrder.forEach { emotion ->
                                    val style = emotionCardStyleMap[emotion]
                                    val count = collectedMap[emotion] ?: 0
                                    val collected = count > 0
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (collected)
                                                            style?.darkColor?.copy(alpha = if (isDark) 1f else 0.15f)
                                                                ?: Color(0xFF2A2A2A)
                                                        else
                                                            if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
                                                    )
                                                    .border(
                                                        1.5.dp,
                                                        if (collected)
                                                            if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF)
                                                        else
                                                            if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .alpha(if (collected) 1f else 0.3f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val lottieRes = style?.lottieRes
                                                if (lottieRes != null && lottieRes != 0) {
                                                    LottieIcon(
                                                        resId = lottieRes,
                                                        size = 36.dp,
                                                        isPlaying = collected
                                                    )
                                                } else {
                                                    Text(text = style?.emoji ?: "", fontSize = 22.sp)
                                                }
                                            }
                                            if (collected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .offset(x = 4.dp, y = (-4).dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = count.toString(),
                                                        fontSize = 8.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = emotion,
                                            fontSize = 9.sp,
                                            color = if (collected)
                                                if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF)
                                            else subTextColor
                                        )
                                    }
                                }
                            }

                            // 바 차트
                            if (collectedMap.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE))
                                Spacer(modifier = Modifier.height(12.dp))

                                val maxCount = collectedMap.values.maxOrNull() ?: 1
                                emotionOrder
                                    .filter { collectedMap.containsKey(it) }
                                    .sortedByDescending { collectedMap[it] ?: 0 }
                                    .forEach { emotion ->
                                        val count = collectedMap[emotion] ?: 0
                                        val ratio = count.toFloat() / maxCount
                                        val barColor = emotionColors[emotion] ?: Color.Gray
                                        val style = emotionCardStyleMap[emotion]

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = style?.emoji ?: "",
                                                fontSize = 14.sp,
                                                modifier = Modifier.width(20.dp)
                                            )
                                            Text(
                                                text = emotion,
                                                fontSize = 10.sp,
                                                color = subTextColor,
                                                modifier = Modifier.width(32.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (isDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)
                                                    )
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(ratio)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(barColor)
                                                )
                                            }
                                            Text(
                                                text = "${count}번",
                                                fontSize = 10.sp,
                                                color = subTextColor,
                                                modifier = Modifier.width(24.dp)
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 그래프 ──
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "일별 감정 그래프",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EmotionLineChart(
                        isDark = isDark,
                        cardBg = cardBg,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        records = homeState.records
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 전체 기록 ──
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "전체 기록",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

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
                        delay(200L + index * 80L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(350)) + slideInVertically(
                            animationSpec = tween(350),
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
        val defaultDates = (4 downTo 0).map { today.minusDays(it.toLong()) }
        val extraDates = dailyMap.keys
            .map { LocalDate.parse(it) }
            .filter { it !in defaultDates }
            .sortedDescending()
        val allDates = (defaultDates + extraDates).sortedBy { it }
        allDates.map { date ->
            val counts = dailyMap[date.toString()] ?: emptyMap()
            GraphPoint(date = "${date.monthValue}/${date.dayOfMonth}", counts = counts)
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
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            if (graphPoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "아직 데이터가 없어요", color = subTextColor, fontSize = 13.sp)
                }
            } else {
                val chartHeight = 200.dp
                val chartWidthPx = remember { mutableStateOf(0f) }
                val progress = animationProgress.value

                Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
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
                        val segmentWidth = if (graphPoints.size > 1) chartW / (graphPoints.size - 1) else chartW / 2f

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

                        for (i in 0..yStepCount) {
                            val y = paddingTop + chartH - (i.toFloat() / yStepCount * chartH)
                            drawLine(
                                color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE),
                                start = androidx.compose.ui.geometry.Offset(leftPadding, y),
                                end = androidx.compose.ui.geometry.Offset(w - rightPadding, y),
                                strokeWidth = 1f
                            )
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawText(i.toString(), leftPadding - 8f, y + 8f, yLabelPaint)
                            }
                        }

                        drawLine(
                            color = if (isDark) Color(0xFF3A3A3A) else Color(0xFFDDDDDD),
                            start = androidx.compose.ui.geometry.Offset(leftPadding, paddingTop),
                            end = androidx.compose.ui.geometry.Offset(leftPadding, paddingTop + chartH),
                            strokeWidth = 1.5f
                        )

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
                                if (value > 0) drawCircle(color = color, radius = 6f, center = points[0])
                                return@forEach
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
                            points.take(fullSegments + 1).forEachIndexed { idx, point ->
                                val value = graphPoints.getOrNull(idx)?.counts?.get(emotion) ?: 0
                                if (value > 0) drawCircle(color = color, radius = 4f, center = point)
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

                    selectedDateIndex?.let { idx ->
                        val data = graphPoints.getOrNull(idx) ?: return@let
                        val w = chartWidthPx.value
                        if (w > 0f) {
                            val leftPadding = w * 0.10f
                            val rightPadding = w * 0.05f
                            val chartW = w - leftPadding - rightPadding
                            val segmentWidth = if (graphPoints.size > 1) chartW / (graphPoints.size - 1) else chartW / 2f
                            val x = if (graphPoints.size == 1) leftPadding + chartW / 2f else leftPadding + idx * segmentWidth
                            val xRatio = x / w

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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(emotionColors[emotion] ?: Color.Gray)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                emotions.forEach { emotion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(emotionColors[emotion] ?: Color.Gray))
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
    val tagColor = if (isDark) style?.darkColor ?: DarkSurface else style?.lightColor ?: LightSurface

    val formattedDate = remember(record.created_at) {
        try { record.created_at.substring(0, 10).replace("-", ".") }
        catch (e: Exception) { record.created_at }
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