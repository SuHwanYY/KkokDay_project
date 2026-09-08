package com.example.kkokday.data.nickname

interface NicknameRepository {

    /** nicknames/{nickname} 문서 존재 여부로 중복 여부를 조회한다. */
    suspend fun isNicknameAvailable(nickname: String): Result<Boolean>

    /**
     * nicknames/{nickname} 문서 생성과 users/{uid} 프로필 생성을 Firestore 트랜잭션으로
     * 원자적으로 처리한다. 트랜잭션 내부에서 닉네임 점유 여부를 다시 확인하므로,
     * [isNicknameAvailable]로 미리 확인했더라도 그 사이 다른 사용자가 선점했다면
     * [NicknameTakenException]으로 실패한다.
     *
     * 이메일 가입/카카오 로그인 양쪽이 공통으로 쓰는 "사용자 최초 생성" 진입점이다.
     * [profileImageUrl]은 카카오처럼 가입 시점에 프로필 사진이 있는 경우에만 넘긴다.
     */
    suspend fun reserveNickname(nickname: String, uid: String, profileImageUrl: String? = null): Result<Unit>

    /**
     * 이미 닉네임이 있는 사용자가 다른 닉네임으로 바꾼다 — 기존 nicknames/{oldNickname}
     * 예약을 반납(delete)하고 nicknames/{newNickname}을 새로 예약하면서 users/{uid}.nickname을
     * 같은 트랜잭션으로 갱신한다. [reserveNickname]과 마찬가지로 트랜잭션 내부에서 다시 한번
     * 중복을 확인하므로, 그 사이 다른 사용자가 선점했다면 [NicknameTakenException]으로 실패한다.
     */
    suspend fun changeNickname(uid: String, oldNickname: String, newNickname: String): Result<Unit>
}

/** 트랜잭션 도중 닉네임이 이미 선점되어 있음을 나타낸다. */
class NicknameTakenException : Exception("Nickname already taken")
