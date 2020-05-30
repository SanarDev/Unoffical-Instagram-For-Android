package com.sanardev.instagrammqtt.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.datasource.remote.HeaderInterceptor
import com.sanardev.instagrammqtt.usecase.UseCase
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule {


    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder().setLenient()
        return gsonBuilder.create()
    }


    @Provides
    @Singleton
    fun provideOkhttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder().apply {
            //            cache(cache)
            addNetworkInterceptor(HeaderInterceptor("Instagram 94.0.0.22.116 Android (24/7.0; 480dpi; 1080x1794; HUAWEI/HONOR; PRA-LA1; HWPRA-H; hi6250; en_US; 155374104)"))
            readTimeout(20, TimeUnit.SECONDS)
            connectTimeout(20, TimeUnit.SECONDS)
        }
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(InstagramConstants.API_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideIbazdidApiService(retrofit: Retrofit): InstagramRemote {
        return retrofit.create(InstagramRemote::class.java)
    }
}