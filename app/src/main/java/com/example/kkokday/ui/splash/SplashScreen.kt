package com.example.kkokday.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kkokday.R
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MILLIS = 2000L

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DELAY_MILLIS)
        when (viewModel.resolveStartDestination()) {
            SplashDestination.HOME -> onNavigateToHome()
            SplashDestination.LOGIN -> onNavigateToLogin()
        }
    }

    SplashScreenContent()
}

@Composable
private fun SplashScreenContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = KkokDayCream
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "콕데이 스플래시",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(KkokDayCream)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    KkokDayTheme {
        SplashScreenContent()
    }
}
