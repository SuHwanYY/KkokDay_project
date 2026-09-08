package com.example.kkokday.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Firebase 예외를 사용자에게 보여줄 한국어 메시지로 변환한다. */
fun Throwable.toAuthErrorMessage(): String = when (this) {
    is FirebaseAuthInvalidUserException -> "존재하지 않는 계정이에요. 이메일을 다시 확인해주세요."
    is FirebaseAuthInvalidCredentialsException -> "이메일 또는 비밀번호가 올바르지 않아요."
    is FirebaseAuthUserCollisionException -> "이미 가입된 이메일이에요."
    is FirebaseAuthWeakPasswordException -> "비밀번호가 너무 약해요. 더 안전한 비밀번호를 사용해주세요."
    is FirebaseAuthActionCodeException -> "링크가 만료되었거나 이미 사용됐어요. 다시 시도해주세요."
    is FirebaseTooManyRequestsException -> "요청이 너무 많아요. 잠시 후 다시 시도해주세요."
    is FirebaseNetworkException -> "네트워크 연결을 확인해주세요."
    else -> "일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요."
}
