package com.example.kkokday.directions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * 장소의 "연락 수단" 액션(전화 걸기 / 상세 페이지 보기)을 담당한다. 전화는 ACTION_DIAL로
 * 다이얼러만 열고 실제 통화 버튼은 사용자가 직접 누르게 해서 CALL_PHONE 권한이 필요 없다.
 */
object ContactActionLauncher {

    fun dial(context: Context, phone: String) {
        if (phone.isBlank()) return
        startSafely(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        startSafely(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun startSafely(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // 다이얼러/브라우저 처리 액티비티가 없는 예외적인 기기에 대한 방어막.
        }
    }
}
