package com.example.kkokday.data.auth

/**
 * 비밀번호 재설정 딥링크 설정.
 *
 * Firebase Dynamic Links는 종료되었고, App Links는 도메인 소유권 인증이 필요해 이 프로젝트
 * 규모에는 과하므로 커스텀 URL 스킴([RESET_PASSWORD_DEEP_LINK])으로 앱을 직접 연다.
 *
 * 다만 Firebase Auth의 sendPasswordResetEmail(actionCodeSettings)에 넘기는 continueUrl은
 * http/https이면서 Firebase 프로젝트의 승인된 도메인이어야 하므로, 커스텀 스킴을 곧바로 넣을 수
 * 없다. 대신 Firebase Hosting 기본 도메인(별도 도메인 인증 불필요)에 아주 작은 리다이렉트 페이지
 * ( /firebase-hosting/reset-redirect.html )를 배포하고, 그 페이지가 받은 쿼리 파라미터
 * (mode, oobCode 등)를 그대로 [RESET_PASSWORD_DEEP_LINK]로 다시 넘겨 앱을 연다.
 *
 * [PASSWORD_RESET_CONTINUE_URL]만 실제 배포 URL로 지정해주면 된다. sendPasswordResetEmail 호출 시
 * ActionCodeSettings.url로 이 값을 직접 넘기기 때문에(AuthRepositoryImpl 참고), Firebase Console의
 * Authentication > Templates > 비밀번호 재설정 > 작업 URL 커스터마이즈 설정은 필요 없다 (설정해도 무시됨).
 * 콘솔에서 이 항목 저장이 EMAIL_TEMPLATE_UPDATE_NOT_ALLOWED 등으로 실패하더라도, 코드에서 URL을
 * 직접 지정하는 이 방식에는 영향이 없다.
 */
object PasswordResetConfig {
    const val RESET_PASSWORD_SCHEME = "kokday"
    const val RESET_PASSWORD_HOST = "resetPassword"
    const val RESET_PASSWORD_DEEP_LINK = "$RESET_PASSWORD_SCHEME://$RESET_PASSWORD_HOST"

    const val PASSWORD_RESET_CONTINUE_URL = "https://kkokday.web.app/reset-redirect.html"
}
