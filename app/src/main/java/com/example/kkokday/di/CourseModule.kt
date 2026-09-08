package com.example.kkokday.di

import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.data.course.CourseRepositoryImpl
import com.example.kkokday.data.course.CourseVoteRepository
import com.example.kkokday.data.course.CourseVoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CourseModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindCourseVoteRepository(impl: CourseVoteRepositoryImpl): CourseVoteRepository
}
