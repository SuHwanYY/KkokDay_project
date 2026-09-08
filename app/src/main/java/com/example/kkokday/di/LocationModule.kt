package com.example.kkokday.di

import com.example.kkokday.data.location.SelectedLocationRepository
import com.example.kkokday.data.location.SelectedLocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindSelectedLocationRepository(impl: SelectedLocationRepositoryImpl): SelectedLocationRepository
}
