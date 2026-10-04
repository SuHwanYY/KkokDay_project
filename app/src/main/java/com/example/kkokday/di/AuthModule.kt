package com.example.kkokday.di

import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.AuthRepositoryImpl
import com.example.kkokday.data.auth.FirstSignupCompletionRepository
import com.example.kkokday.data.auth.FirstSignupCompletionRepositoryImpl
import com.example.kkokday.data.auth.PendingEmailSignupRepository
import com.example.kkokday.data.auth.PendingEmailSignupRepositoryImpl
import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.data.nickname.NicknameRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNicknameRepository(impl: NicknameRepositoryImpl): NicknameRepository

    @Binds
    @Singleton
    abstract fun bindPendingEmailSignupRepository(impl: PendingEmailSignupRepositoryImpl): PendingEmailSignupRepository

    @Binds
    @Singleton
    abstract fun bindFirstSignupCompletionRepository(
        impl: FirstSignupCompletionRepositoryImpl,
    ): FirstSignupCompletionRepository
}
