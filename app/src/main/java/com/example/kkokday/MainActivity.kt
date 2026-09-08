package com.example.kkokday

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.kkokday.navigation.KkokDayNavHost
import com.example.kkokday.ui.theme.KkokDayTheme
import dagger.hilt.android.AndroidEntryPoint

private const val SPLASH_ICON_EXIT_DURATION_MILLIS = 220L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 시스템 스플래시 아이콘(splash_logo_icon)은 래스터 PNG라 플랫폼이 지원하는
        // AnimatedVectorDrawable 자동 등장 애니메이션은 쓸 수 없다 — 대신 공식
        // OnExitAnimationListener로 "사라지는 순간"을 살짝 페이드+스케일로 다듬어서,
        // 시스템 스플래시에서 splash.png 화면으로의 전환이 뚝 끊기지 않게 한다.
        splashScreen.setOnExitAnimationListener { provider ->
            provider.iconView.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(SPLASH_ICON_EXIT_DURATION_MILLIS)
                .withEndAction { provider.remove() }
                .start()
        }

        setContent {
            KkokDayTheme {
                val controller = rememberNavController()
                navController = controller

                LaunchedEffect(controller) {
                    handleDeepLinkIntent(intent, controller)
                }

                KkokDayNavHost(navController = controller)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.let { handleDeepLinkIntent(intent, it) }
    }

    private fun handleDeepLinkIntent(intent: Intent?, controller: NavHostController) {
        if (intent?.data != null) {
            controller.handleDeepLink(intent)
        }
    }
}
