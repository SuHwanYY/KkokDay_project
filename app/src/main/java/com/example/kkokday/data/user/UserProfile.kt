package com.example.kkokday.data.user

/** `users/{uid}` 문서의 프로필 필드. */
data class UserProfile(
    val nickname: String,
    val profileImageUrl: String?,
)
