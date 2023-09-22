package com.android.services.di.module

import com.android.services.network.api.*
import com.android.services.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkApiModule {

    @Provides
    @Singleton
    fun provideSipActivateNodeServer(
        okHttpClient: OkHttpClient,
        @Named(AppConstants.NODE_VIEW360_SERVER_TAG) baseUrl: String,
    ): View360Api {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(View360Api::class.java)
    }


    @Provides
    @Singleton
    fun provideNodeServerApi(
        okHttpClient: OkHttpClient,
        @Named(AppConstants.NODE_SERVER_TAG) baseUrl: String,
    ): TOSApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(TOSApi::class.java)
    }
}
