package com.example.kkokday.di

import com.example.kkokday.BuildConfig
import com.example.kkokday.data.network.KakaoLocalApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

private const val KAKAO_LOCAL_BASE_URL = "https://dapi.kakao.com/"

@Module
@InstallIn(SingletonComponent::class)
object KakaoLocalNetworkModule {

    @Provides
    @Singleton
    fun provideKakaoLocalApiService(): KakaoLocalApiService {
        val json = Json { ignoreUnknownKeys = true }
        val restApiKey = BuildConfig.KAKAO_REST_API_KEY
        check(restApiKey.isNotBlank()) {
            "local.properties에 KAKAO_REST_API_KEY가 설정돼 있지 않아요."
        }

        // REST API 키는 여기 한 곳(인터셉터)에서만 읽어 헤더에 실을 뿐, 호출부 코드나
        // 로그에는 절대 노출되지 않는다.
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "KakaoAK $restApiKey")
                .build()
            chain.proceed(request)
        }
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(KAKAO_LOCAL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(KakaoLocalApiService::class.java)
    }
}
