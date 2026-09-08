package com.example.kkokday.di

import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseFunctionsModule {

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions =
        // Cloud Functions를 asia-northeast3(서울) 리전에 배포했으므로, 클라이언트도 같은
        // 리전을 명시해야 한다. 리전을 안 맞추면 기본값(us-central1)으로 호출을 시도해서
        // NOT_FOUND 에러가 난다.
        FirebaseFunctions.getInstance("asia-northeast3")
}
