package com.example.kkokday.data.user

import android.net.Uri

interface UserProfileRepository {

    /** `users/{uid}` 문서에서 마이 화면 프로필 카드에 쓸 nickname/profileImageUrl을 읽는다. */
    suspend fun getUserProfile(uid: String): Result<UserProfile>

    /**
     * 갤러리에서 고른 사진을 압축해 Storage에 올리고 `users/{uid}.profileImageUrl`을
     * 갱신한다. 매번 같은 파일 경로(`profileImages/{uid}/avatar.jpg`)에 덮어써서, 이전
     * 파일을 별도로 지우지 않아도 자연스럽게 교체된다.
     */
    suspend fun updateProfileImage(uid: String, imageUri: Uri): Result<String>

    /**
     * Cloud Functions `deleteAccount`를 호출해 서버에서 계정과 관련 데이터를 전부 삭제한다.
     * 성공하면 Firebase Auth 계정도 이미 삭제된 상태이므로, 호출부는 로컬 세션만 정리하면 된다.
     */
    suspend fun deleteAccount(): Result<Unit>
}
