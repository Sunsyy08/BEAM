package com.project.beam.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmotionBottomSheet(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit = {},
    isLoading: Boolean = false,
    hint: String? = null
) {
    val bgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText
    val inputBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
    val btnBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE)

    var text by remember { mutableStateOf("") }
    val maxLength = 300

    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current


    val isEnabled = text.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF555555) else Color(0xFFDDDDDD))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // ── 헤더 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "새로운 기록",
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                    Text(
                        text = "오늘 뭐가 남았나요?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(btnBg),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onDismiss) {
                        Text(text = "×", fontSize = 18.sp, color = subTextColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            hint?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F0FF))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💬 $it",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF9B8EC4) else Color(0xFF7C4DFF),
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── 텍스트 입력 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(inputBg)
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF3A3A3A) else Color(0xFFEEEEEE),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                TextField(
                    value = text,
                    onValueChange = { if (it.length <= maxLength) text = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Text(
                            text = "오늘 어떤 감정을 여기 두고 갈까요...",
                            color = subTextColor,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    maxLines = 6
                )

                // 글자 수
                Text(
                    text = "${text.length} / $maxLength",
                    fontSize = 11.sp,
                    color = subTextColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 기록하기 버튼 ──
            Button(
                onClick = {
                    if (isEnabled && !isLoading) {
                        onSubmit(text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) {
                        if (isDark) DarkText else LightText
                    } else {
                        btnBg
                    }
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (isDark) DarkBackground else LightBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "기록하기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) {
                            if (isDark) DarkBackground else LightBackground
                        } else {
                            textColor
                        }
                    )
                }
            }
        }
    }
}