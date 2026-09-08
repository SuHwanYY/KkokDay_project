package com.example.kkokday.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        // 인증 메일 제목/본문과, 이메일 인증 링크 클릭 시 뜨는 Firebase 기본 안내 페이지의
        // 언어를 한국어로 고정한다.
        setLanguageCode("ko")
    }
}
