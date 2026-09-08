package com.example.kkokday.di

import com.example.kkokday.data.kakao.KakaoAuthRepository
import com.example.kkokday.data.kakao.KakaoAuthRepositoryImpl
import com.example.kkokday.data.kakao.KakaoFirebaseBridgeRepository
import com.example.kkokday.data.kakao.KakaoFirebaseBridgeRepositoryImpl
import com.example.kkokday.data.kakao.KakaoUserRepository
import com.example.kkokday.data.kakao.KakaoUserRepositoryImpl
import com.example.kkokday.data.session.KakaoSessionRepository
import com.example.kkokday.data.session.KakaoSessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KakaoModule {

    @Binds
    @Singleton
    abstract fun bindKakaoAuthRepository(impl: KakaoAuthRepositoryImpl): KakaoAuthRepository

    @Binds
    @Singleton
    abstract fun bindKakaoUserRepository(impl: KakaoUserRepositoryImpl): KakaoUserRepository

    @Binds
    @Singleton
    abstract fun bindKakaoFirebaseBridgeRepository(
        impl: KakaoFirebaseBridgeRepositoryImpl,
    ): KakaoFirebaseBridgeRepository

    @Binds
    @Singleton
    abstract fun bindKakaoSessionRepository(impl: KakaoSessionRepositoryImpl): KakaoSessionRepository
}
