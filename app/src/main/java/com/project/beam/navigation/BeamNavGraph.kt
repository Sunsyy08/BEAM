package com.project.beam.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.beam.R
import com.project.beam.ui.archive.ArchiveScreen
import com.project.beam.ui.auth.LoginScreen
import com.project.beam.ui.home.EmotionDetailScreen
import com.project.beam.ui.home.HomeScreen
import com.project.beam.ui.home.LottieIcon
import com.project.beam.ui.theme.*
import com.project.beam.viewmodel.EmotionCardUi

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Archive : Screen("archive")
    object EmotionDetail : Screen("emotion_detail")}

@Composable
fun BeamNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val systemDark = isSystemInDarkTheme()
    var isDark by remember { mutableStateOf(systemDark) }

    // 바텀바 아이콘 애니메이션 상태 (전역)
    var homeClicked by remember { mutableStateOf(false) }
    var addClicked by remember { mutableStateOf(false) }
    var timelineClicked by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 바텀바 숨길 화면
    val showBottomBar = currentRoute != Screen.Login.route

    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) DarkText else LightText
    val subTextColor = if (isDark) DarkSubText else LightSubText

    var selectedEmotionCard by remember { mutableStateOf<com.project.beam.viewmodel.EmotionCardUi?>(null) }

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            if (showBottomBar) {
                BeamBottomBar(
                    isDark = isDark,
                    bgColor = bgColor,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    currentRoute = currentRoute,
                    homeClicked = homeClicked,
                    addClicked = addClicked,
                    timelineClicked = timelineClicked,
                    onHomeClick = {
                        homeClicked = !homeClicked
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onAddClick = {
                        addClicked = !addClicked
                        showBottomSheet = true
                    },
                    onArchiveClick = {
                        timelineClicked = !timelineClicked
                        navController.navigate(Screen.Archive.route) {
                            popUpTo(Screen.Archive.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onGoogleSignInClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    isDark = isDark,
                    onDarkModeToggle = { isDark = it },
                    showBottomSheet = showBottomSheet,
                    onBottomSheetDismiss = { showBottomSheet = false },
                    onSloganClick = { showBottomSheet = true },
                    onEmotionClick = { card ->
                        selectedEmotionCard = card
                        navController.navigate(Screen.EmotionDetail.route)
                    }
                )
            }

            composable(Screen.EmotionDetail.route) {
                selectedEmotionCard?.let { card ->
                    EmotionDetailScreen(
                        emotionCardUi = card,
                        isDark = isDark,
                        onDarkModeToggle = { isDark = it },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Archive.route) {
                ArchiveScreen(
                    isDark = isDark,
                    onDarkModeToggle = { isDark = it }
                )
            }
        }
    }
}

// ── 공통 바텀바 ───────────────────────────────
@Composable
fun BeamBottomBar(
    isDark: Boolean,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    subTextColor: androidx.compose.ui.graphics.Color,
    currentRoute: String?,
    homeClicked: Boolean,
    addClicked: Boolean,
    timelineClicked: Boolean,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
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
                modifier = Modifier.clickable { onHomeClick() }
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
                    color = if (currentRoute == Screen.Home.route) textColor else subTextColor,
                    fontWeight = if (currentRoute == Screen.Home.route) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            // 유물 등록
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-8).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DarkText else LightText)
                        .clickable { onAddClick() },
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

            // 연대기
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onArchiveClick() }
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
                    color = if (currentRoute == Screen.Archive.route) textColor else subTextColor,
                    fontWeight = if (currentRoute == Screen.Archive.route) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}