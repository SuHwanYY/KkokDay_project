package com.example.kkokday.di

import com.example.kkokday.data.user.BlockedAuthorRepository
import com.example.kkokday.data.user.BlockedAuthorRepositoryImpl
import com.example.kkokday.data.user.HomePinsRepository
import com.example.kkokday.data.user.HomePinsRepositoryImpl
import com.example.kkokday.data.user.UserProfileRepository
import com.example.kkokday.data.user.UserProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun bindHomePinsRepository(impl: HomePinsRepositoryImpl): HomePinsRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindBlockedAuthorRepository(impl: BlockedAuthorRepositoryImpl): BlockedAuthorRepository
}
