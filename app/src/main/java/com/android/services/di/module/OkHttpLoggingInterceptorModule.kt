package com.android.services.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor

@InstallIn(SingletonComponent::class)
@Module
abstract class OkHttpLoggingInterceptorModule {

    @Binds
    abstract fun bindOkHttpLoggingInterceptor(okHttpLoggingInterceptor: OkHttpLoggingInterceptor): Interceptor
}