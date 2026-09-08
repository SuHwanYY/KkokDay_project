package com.example.kkokday.ui.course

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * Firebase Hosting에 배포한 코스 보기 전용 웹페이지("firebase-hosting/course.html") 기준
 * 공유 링크 — 앱이 없는 사람도 이 URL을 열면 코스 내용을 볼 수 있고, 투표도 여기서 한다.
 */
private const val COURSE_SHARE_BASE_URL = "https://kkokday.web.app/course.html"

fun courseShareUrl(courseId: String): String = "$COURSE_SHARE_BASE_URL?courseId=$courseId"

/**
 * 코스 상세/투표 상세 화면이 공유하는 공유 로직 — 링크를 클립보드에 복사하고 Android 공유
 * 시트를 띄운다. 호출부는 성공 스낵바("링크를 복사했어요")를 직접 띄운다.
 */
fun shareCourse(context: Context, clipboardManager: ClipboardManager, courseId: String) {
    val shareUrl = courseShareUrl(courseId)
    clipboardManager.setText(AnnotatedString(shareUrl))
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareUrl)
            },
            "코스 공유하기",
        ),
    )
}
