package com.example.kkokday.ui.auth.validation

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$")

const val MIN_PASSWORD_LENGTH = 8
const val MIN_NICKNAME_LENGTH = 2
const val MAX_NICKNAME_LENGTH = 10

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

fun isValidSignupPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

fun isValidNickname(nickname: String): Boolean =
    nickname.trim().length in MIN_NICKNAME_LENGTH..MAX_NICKNAME_LENGTH

/** 입력값이 비어있지 않을 때만 에러 메시지를 반환한다 (빈 칸일 때는 아직 에러로 표시하지 않음). */
fun emailErrorOrNull(email: String): String? =
    if (email.isNotBlank() && !isValidEmail(email)) "올바른 이메일 형식이 아니에요" else null

fun signupPasswordErrorOrNull(password: String): String? =
    if (password.isNotBlank() && !isValidSignupPassword(password)) {
        "비밀번호는 ${MIN_PASSWORD_LENGTH}자 이상이어야 해요"
    } else {
        null
    }

fun passwordConfirmErrorOrNull(password: String, passwordConfirm: String): String? =
    if (passwordConfirm.isNotBlank() && passwordConfirm != password) "비밀번호가 일치하지 않아요" else null

fun nicknameErrorOrNull(nickname: String): String? =
    if (nickname.isNotBlank() && !isValidNickname(nickname)) {
        "닉네임은 ${MIN_NICKNAME_LENGTH}~${MAX_NICKNAME_LENGTH}자로 입력해주세요"
    } else {
        null
    }
