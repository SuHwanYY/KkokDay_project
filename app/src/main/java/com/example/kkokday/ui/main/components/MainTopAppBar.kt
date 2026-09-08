package com.example.kkokday.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kkokday.R
import com.example.kkokday.ui.theme.KkokDayMainTextDark

/**
 * 홈 화면 상단 앱바. "콕데이" 로고(ic_main) + 타이틀 텍스트만 보여준다.
 *
 * 상태바 인셋은 [com.example.kkokday.navigation.KkokDayNavHost]의 insetSafeComposable이
 * 화면 진입 시 한 번만 처리하므로, 여기서는 따로 statusBarsPadding을 넣지 않는다.
 */
@Composable
fun MainTopAppBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_main),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "콕데이",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = KkokDayMainTextDark,
        )
    }
}
