package com.example.kkokday.data.kakao

import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause

/** 사용자가 로그인 화면(카카오톡/카카오계정)에서 스스로 취소한 경우. 에러 메시지를 보여줄 필요가 없다. */
fun Throwable.isKakaoLoginCancelled(): Boolean =
    this is ClientError && reason == ClientErrorCause.Cancelled

fun Throwable.toKakaoErrorMessage(): String = when (this) {
    is KakaoLoginInProgressException -> "이전 카카오 로그인 창이 아직 열려 있어요. 그 창을 닫거나 완료한 뒤 다시 시도해주세요."
    else -> "카카오 로그인 중 오류가 발생했어요. 잠시 후 다시 시도해주세요."
}
