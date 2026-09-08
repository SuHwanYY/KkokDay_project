package com.example.kkokday.di

import com.example.kkokday.data.place.CategoryPlaceRepository
import com.example.kkokday.data.place.CategoryPlaceRepositoryImpl
import com.example.kkokday.data.place.FavoriteRepository
import com.example.kkokday.data.place.FavoriteRepositoryImpl
import com.example.kkokday.data.place.PlaceSearchRepository
import com.example.kkokday.data.place.PlaceSearchRepositoryImpl
import com.example.kkokday.data.place.RecentPlaceRepository
import com.example.kkokday.data.place.RecentPlaceRepositoryImpl
import com.example.kkokday.data.review.PlaceReviewRepository
import com.example.kkokday.data.review.PlaceReviewRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaceModule {

    @Binds
    @Singleton
    abstract fun bindPlaceSearchRepository(impl: PlaceSearchRepositoryImpl): PlaceSearchRepository

    @Binds
    @Singleton
    abstract fun bindRecentPlaceRepository(impl: RecentPlaceRepositoryImpl): RecentPlaceRepository

    @Binds
    @Singleton
    abstract fun bindCategoryPlaceRepository(impl: CategoryPlaceRepositoryImpl): CategoryPlaceRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindPlaceReviewRepository(impl: PlaceReviewRepositoryImpl): PlaceReviewRepository
}
