package com.project.beam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.beam.data.emotion.EmotionCard
import com.project.beam.ui.auth.LoginScreen
import com.project.beam.ui.home.EmotionDetailScreen
import com.project.beam.ui.home.HomeScreen
import com.project.beam.ui.home.sampleEmotions

// ── 라우트 정의 ──────────────────────────────
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object EmotionDetail : Screen("emotion_detail/{emotionIndex}") {
        fun createRoute(index: Int) = "emotion_detail/$index"
    }
}

// ── NavGraph ──────────────────────────────────
@Composable
fun BeamNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 로그인
        composable(Screen.Login.route) {
            LoginScreen(
                onGoogleSignInClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 홈
        composable(Screen.Home.route) {
            HomeScreen(
                onEmotionClick = { index ->
                    navController.navigate(Screen.EmotionDetail.createRoute(index))
                },
                onAddClick = { },
                onArchiveClick = { }
            )
        }

        // 감정 상세
        composable(Screen.EmotionDetail.route) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("emotionIndex")?.toIntOrNull() ?: 0
            val emotion = sampleEmotions[index]
            EmotionDetailScreen(
                emotion = emotion,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}