package com.example.kkokday.ui.auth.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.theme.KkokDayOrange

/** Firebase Hosting에 배포한 정적 페이지("firebase-hosting/terms.html"/"privacy.html") URL. */
const val TERMS_OF_SERVICE_URL = "https://kkokday.web.app/terms.html"
const val PRIVACY_POLICY_URL = "https://kkokday.web.app/privacy.html"

/**
 * [onViewClick]은 [label] 끝에 붙는 밑줄 처리된 "보기" 링크의 클릭 핸들러다. 이 링크는 별도
 * clickable 영역이라, 탭하면 체크박스는 토글되지 않고 onViewClick만 호출된다(부모 Row의
 * toggleable보다 안쪽 clickable이 먼저 제스처를 소비하는 Compose 기본 동작에 기댄다).
 */
@Composable
fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = KkokDayOrange),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = "보기",
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = TextDecoration.Underline,
            color = KkokDayOrange,
            modifier = Modifier
                .clickable(onClick = onViewClick)
                .padding(start = 8.dp),
        )
    }
}
