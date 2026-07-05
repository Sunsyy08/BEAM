package com.project.beam.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.beam.ui.theme.*
import com.project.beam.R

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit = {}
) {
    val isDark       = isSystemInDarkTheme()
    val bgColor      = if (isDark) DarkBackground       else LightBackground
    val textColor    = if (isDark) DarkText             else LightText
    val subTextColor = if (isDark) DarkSubText          else LightSubText
    val btnColor     = if (isDark) GoogleBtnDark        else GoogleBtnLight
    val btnBorder    = if (isDark) GoogleBtnBorderDark  else GoogleBtnBorderLight
    val logoBg       = if (isDark) LogoBgDark           else LogoBgLight
    val logoText     = if (isDark) LogoTextDark         else LogoTextLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1.5f))

            // ── 로고 ──
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "BEAM 로고",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "오늘의 감정을 여기 두고 가요",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = subTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── 구글 로그인 버튼 ──
            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(
                        width = 1.dp,
                        color = btnBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnColor
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Text(
                    text = "G",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google로 계속하기",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 하단 안내 ──
            Text(
                text = "계속 진행 시 이용약관 및 개인정보처리방침에 동의하게 됩니다.",
                fontSize = 11.sp,
                color = subTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

        }
    }
}

@Preview(showBackground = true, name = "Login Light")
@Composable
fun LoginScreenLightPreview() {
    LoginScreen()
}

@Preview(showBackground = true, backgroundColor = 0xFF141414, name = "Login Dark")
@Composable
fun LoginScreenDarkPreview() {
    LoginScreen()
}